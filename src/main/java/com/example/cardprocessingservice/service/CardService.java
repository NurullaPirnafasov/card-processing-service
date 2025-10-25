package com.example.cardprocessingservice.service;

import com.example.cardprocessingservice.dto.*;
import com.example.cardprocessingservice.exception.*;
import com.example.cardprocessingservice.mapper.CardMapper;
import com.example.cardprocessingservice.mapper.TransactionMapper;
import com.example.cardprocessingservice.model.entity.*;
import com.example.cardprocessingservice.model.enums.*;
import com.example.cardprocessingservice.model.enums.Currency;
import com.example.cardprocessingservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CardService {

    private static final Logger log = LogManager.getLogger(CardService.class);

    private final CardRepository cardRepository;
    private final AuthUserRepository authUserRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final TransactionRepository transactionRepository;
    private final CardMapper cardMapper;
    private final TransactionMapper transactionMapper;
    private final CurrencyService currencyService; // external API uchun

    @Transactional
    public CardResponse createCard(CardCreateRequest request, String idempotencyKeyValue) {
        log.info("Creating new card for userId={} with idempotencyKey={}", request.getUser_id(), idempotencyKeyValue);

        Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findByKey(idempotencyKeyValue);
        if (existingKey.isPresent()) {
            log.info("Idempotency key {} already exists, returning existing card", idempotencyKeyValue);
            String cardId = existingKey.get().getResponseData();
            Card card = cardRepository.findById(cardId)
                    .orElseThrow(() -> {
                        log.error("Card not found for existing idempotency key {}", idempotencyKeyValue);
                        return new ResourceNotFoundException("Card not found for idempotency key");
                    });
            return cardMapper.toResponse(card);
        }

        AuthUser user = authUserRepository.findById(request.getUser_id())
                .orElseThrow(() -> {
                    log.error("User not found with id={}", request.getUser_id());
                    return new ResourceNotFoundException("User not found");
                });

        List<Card> activeCards = cardRepository.findByAuthUserAndStatusNot(user, CardStatus.CLOSED);
        if (activeCards.size() >= 3) {
            log.warn("User {} already has {} active cards", user.getId(), activeCards.size());
            throw new LimitExceededException("User already has maximum 3 non-closed cards");
        }

        Card card = new Card();
        card.setAuthUser(user);
        cardMapper.updateCardFromRequest(request, card);
        Card saved = cardRepository.save(card);
        log.info("New card created successfully with id={}", saved.getId());

        IdempotencyKey key = new IdempotencyKey();
        key.setKey(idempotencyKeyValue);
        key.setResource("CARD_CREATE");
        key.setResponseData(saved.getId());
        idempotencyKeyRepository.save(key);

        return cardMapper.toResponse(saved);
    }

    public Card getCardById(String cardId) {
        log.info("Fetching card by id={}", cardId);
        return cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Card not found for id={}", cardId);
                    return new ResourceNotFoundException("Card with such id not exists in processing");
                });
    }

    @Transactional
    public void blockCard(String cardId, String ifMatch) {
        log.info("Request to block card id={}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Card not found for id={}", cardId);
                    return new ResourceNotFoundException("Card not found");
                });

        String currentTag = "\"" + card.getVersion() + "\"";

        if (!currentTag.equals(ifMatch)) {
            log.warn("ETag mismatch for card id={}", cardId);
            throw new PreconditionFailedException("ETag mismatch - the card data has changed since last read");
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            log.warn("Attempted to block non-active card id={}, status={}", cardId, card.getStatus());
            throw new BadRequestException("Only ACTIVE cards can be blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
        log.info("Card {} successfully blocked", cardId);
    }

    public void unblockCard(String cardId, String ifMatch) {
        log.info("Request to unblock card id={}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Card not found for id={}", cardId);
                    return new ResourceNotFoundException("Card not found");
                });

        String currentTag = "\"" + card.getVersion() + "\"";

        if (!currentTag.equals(ifMatch)) {
            log.warn("ETag mismatch for card id={}", cardId);
            throw new PreconditionFailedException("ETag mismatch - the card data has changed since last read");
        }

        if (card.getStatus() != CardStatus.BLOCKED) {
            log.warn("Attempted to unblock non-blocked card id={}, status={}", cardId, card.getStatus());
            throw new BadRequestException("Only BLOCKED cards can be unblocked");
        }

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
        log.info("Card {} successfully unblocked", cardId);
    }

    @Transactional
    public TransactionResponseDebit withdrawFunds(String cardId, CardDebitRequest request, String idempotencyKey) {
        log.info("Withdraw request for cardId={} amount={} {}", cardId, request.getAmount(), request.getCurrency());

        Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findByKey(idempotencyKey);
        if (existingKey.isPresent()) {
            log.info("Idempotency key {} already exists, returning existing transaction", idempotencyKey);
            String txId = existingKey.get().getResponseData();
            Transaction transaction = transactionRepository.findById(txId)
                    .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for idempotency key"));
            return transactionMapper.toResponseDebit(transaction);
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Card not found for id={}", cardId);
                    return new ResourceNotFoundException("Card not found");
                });

        if (card.getStatus() != CardStatus.ACTIVE) {
            log.warn("Card id={} is not active, cannot withdraw", cardId);
            throw new BadRequestException("Only ACTIVE cards can be debited");
        }

        if (request.getExternal_id() == null) {
            log.error("Withdraw request missing external_id");
            throw new BadRequestException("Missing required field(s)");
        }

        BigDecimal exchangeRate = BigDecimal.ONE;
        BigDecimal amountToWithdraw = BigDecimal.valueOf(request.getAmount());

        if (!request.getCurrency().equals(card.getCurrency())) {
            try {
                exchangeRate = currencyService.getExchangeRate(request.getCurrency(), card.getCurrency());
                log.info("Exchange rate from {} to {} = {}", request.getCurrency(), card.getCurrency(), exchangeRate);
            } catch (Exception e) {
                log.error("Failed to get exchange rate, using fallback", e);
                exchangeRate = BigDecimal.valueOf(12500L);
            }
            amountToWithdraw = amountToWithdraw.multiply(exchangeRate);
        }

        if (card.getBalance().compareTo(amountToWithdraw) < 0) {
            log.warn("Insufficient funds on card id={} balance={} requested={}", cardId, card.getBalance(), amountToWithdraw);
            throw new InsufficientFundsException("Insufficient funds on card");
        }

        card.setBalance(card.getBalance().subtract(amountToWithdraw));
        Transaction tx = new Transaction();
        tx.setCard(card);
        tx.setExternalId(request.getExternal_id());
        tx.setType(TransactionType.DEBIT);
        tx.setPurpose(request.getPurpose());
        tx.setAmount(BigDecimal.valueOf(request.getAmount()));
        tx.setAfterBalance(card.getBalance());
        tx.setCurrency(card.getCurrency());
        tx.setExchangeRate(exchangeRate.longValue());

        Transaction savedTx = transactionRepository.save(tx);
        cardRepository.save(card);
        log.info("Withdraw successful for cardId={} transactionId={}", cardId, savedTx.getId());

        IdempotencyKey key = new IdempotencyKey();
        key.setKey(idempotencyKey);
        key.setResource("CARD_DEBIT");
        key.setResponseData(savedTx.getId());
        idempotencyKeyRepository.save(key);

        return transactionMapper.toResponseDebit(savedTx);
    }

    @Transactional
    public TransactionResponseCredit topUpFunds(String cardId, CardCreditRequest request, String idempotencyKey) {
        log.info("Top-up request for cardId={} amount={} {}", cardId, request.getAmount(), request.getCurrency());

        Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findByKey(idempotencyKey);
        if (existingKey.isPresent()) {
            log.info("Idempotency key {} already exists, returning existing transaction", idempotencyKey);
            String txId = existingKey.get().getResponseData();
            Transaction transaction = transactionRepository.findById(txId)
                    .orElseThrow(() -> new ResourceNotFoundException("Transaction not found for idempotency key"));
            return transactionMapper.toResponseCredit(transaction);
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Card not found for id={}", cardId);
                    return new ResourceNotFoundException("Card not found");
                });

        if (card.getStatus() != CardStatus.ACTIVE) {
            log.warn("Card id={} is not active, cannot top-up", cardId);
            throw new BadRequestException("Only ACTIVE cards can be credited");
        }

        if (request.getExternal_id() == null) {
            log.error("Top-up request missing external_id");
            throw new BadRequestException("Missing required field(s)");
        }

        BigDecimal exchangeRate = BigDecimal.ONE;
        BigDecimal amountToCredit = BigDecimal.valueOf(request.getAmount());

        if (!request.getCurrency().equals(card.getCurrency())) {
            try {
                exchangeRate = currencyService.getExchangeRate(request.getCurrency(), card.getCurrency());
                log.info("Exchange rate from {} to {} = {}", request.getCurrency(), card.getCurrency(), exchangeRate);
            } catch (Exception e) {
                log.error("Failed to get exchange rate, using fallback", e);
                exchangeRate = BigDecimal.valueOf(12500L);
            }
            amountToCredit = amountToCredit.multiply(exchangeRate);
        }

        card.setBalance(card.getBalance().add(amountToCredit));
        cardRepository.save(card);
        log.info("Top-up successful for cardId={} newBalance={}", cardId, card.getBalance());

        Transaction tx = new Transaction();
        tx.setCard(card);
        tx.setExternalId(request.getExternal_id());
        tx.setType(TransactionType.CREDIT);
        tx.setPurpose(TransactionPurpose.TOP_UP);
        tx.setAmount(BigDecimal.valueOf(request.getAmount()));
        tx.setAfterBalance(card.getBalance());
        tx.setCurrency(card.getCurrency());
        tx.setExchangeRate(exchangeRate.longValue());

        Transaction savedTx = transactionRepository.save(tx);

        IdempotencyKey key = new IdempotencyKey();
        key.setKey(idempotencyKey);
        key.setResource("CARD_CREDIT");
        key.setResponseData(savedTx.getId());
        idempotencyKeyRepository.save(key);

        return transactionMapper.toResponseCredit(savedTx);
    }

    @Transactional(readOnly = true)
    public TransactionPageResponse getTransactionHistory(String cardId,
                                                         TransactionType type,
                                                         String transactionId,
                                                         String externalId,
                                                         Currency currency,
                                                         int page,
                                                         int size) {
        log.info("Fetching transaction history for cardId={} page={} size={}", cardId, page, size);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> {
                    log.error("Card not found for id={}", cardId);
                    return new ResourceNotFoundException("Card not found");
                });

        Pageable pageable = PageRequest.of(page, size);

        Page<Transaction> transactions = transactionRepository.findAllByFilters(
                card,
                type,
                transactionId,
                externalId,
                currency,
                pageable
        );

        log.debug("Fetched {} transactions for cardId={}", transactions.getTotalElements(), cardId);

        List<Object> content = transactions.getContent().stream().map(tx -> {
            if (tx.getType() == TransactionType.DEBIT) {
                return transactionMapper.toResponseDebit(tx);
            } else {
                return transactionMapper.toResponseCredit(tx);
            }
        }).toList();

        TransactionPageResponse response = new TransactionPageResponse();
        response.setPage(transactions.getNumber());
        response.setSize(transactions.getSize());
        response.setTotal_pages(transactions.getTotalPages());
        response.setTotal_items(transactions.getTotalElements());
        response.setContent(content);

        return response;
    }
}
