package com.example.cardprocessingservice.service;

import com.example.cardprocessingservice.dto.CardCreateRequest;
import com.example.cardprocessingservice.dto.CardResponse;
import com.example.cardprocessingservice.exception.LimitExceededException;
import com.example.cardprocessingservice.exception.ResourceNotFoundException;
import com.example.cardprocessingservice.mapper.CardMapper;
import com.example.cardprocessingservice.model.entity.AuthUser;
import com.example.cardprocessingservice.model.entity.Card;
import com.example.cardprocessingservice.model.entity.IdempotencyKey;
import com.example.cardprocessingservice.model.enums.CardStatus;
import com.example.cardprocessingservice.repository.AuthUserRepository;
import com.example.cardprocessingservice.repository.CardRepository;
import com.example.cardprocessingservice.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final AuthUserRepository authUserRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final CardMapper cardMapper;

    @Transactional
    public CardResponse createCard(CardCreateRequest request, String idempotencyKeyValue) {

        Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findByKey(idempotencyKeyValue);
        if (existingKey.isPresent()) {
            String cardId = existingKey.get().getResponseData();
            Card card = cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException("Card not found for idempotency key"));
            return cardMapper.toResponse(card);
        }

        AuthUser user = authUserRepository.findById(request.getUser_id())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Card> activeCards = cardRepository.findByAuthUserAndStatusNot(user, CardStatus.CLOSED);
        if (activeCards.size() >= 3) {
            throw new LimitExceededException("User already has maximum 3 non-closed cards");
        }

        Card card = new Card();
        card.setAuthUser(user);
        cardMapper.updateCardFromRequest(request, card);
        Card saved = cardRepository.save(card);

        IdempotencyKey key = new IdempotencyKey();
        key.setKey(idempotencyKeyValue);
        key.setResource("CARD_CREATE");
        key.setResponseData(saved.getId());
        idempotencyKeyRepository.save(key);

        return cardMapper.toResponse(saved);
    }
}
