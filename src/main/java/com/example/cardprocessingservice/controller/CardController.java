package com.example.cardprocessingservice.controller;

import com.example.cardprocessingservice.dto.*;
import com.example.cardprocessingservice.mapper.CardMapper;
import com.example.cardprocessingservice.model.entity.Card;
import com.example.cardprocessingservice.model.enums.Currency;
import com.example.cardprocessingservice.model.enums.TransactionType;
import com.example.cardprocessingservice.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final CardMapper cardMapper;

    @PostMapping
    public ResponseEntity<CardResponse> createCard(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CardCreateRequest request
    ) {
        CardResponse response = cardService.createCard(request, idempotencyKey);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponse> getCard(@PathVariable String cardId) {
        Card card = cardService.getCardById(cardId);
        CardResponse response = cardMapper.toResponse(card);
        String eTag = card.getVersion().toString();
        return ResponseEntity.ok()
                .eTag(eTag)
                .body(response);
    }

    @PostMapping("/{cardId}/block")
    public ResponseEntity<Void> blockCard(
            @PathVariable String cardId,
            @RequestHeader("If-Match") String ifMatch
    ) {
        cardService.blockCard(cardId, ifMatch);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cardId}/unblock")
    public ResponseEntity<Void> unblockCard(
            @PathVariable String cardId,
            @RequestHeader("If-Match") String ifMatch
    ) {
        cardService.unblockCard(cardId, ifMatch);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{cardId}/debit")
    public ResponseEntity<TransactionResponseDebit> withdrawFunds(
            @PathVariable String cardId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CardDebitRequest request
    ) {
        TransactionResponseDebit response = cardService.withdrawFunds(cardId, request, idempotencyKey);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{cardId}/credit")
    public ResponseEntity<TransactionResponseCredit> creditCard(
            @PathVariable String cardId,
            @RequestBody CardCreditRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        TransactionResponseCredit response = cardService.topUpFunds(cardId, request, idempotencyKey);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cardId}/transactions")
    public ResponseEntity<TransactionPageResponse> getTransactionHistory(
            @PathVariable String cardId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String transaction_id,
            @RequestParam(required = false) String external_id,
            @RequestParam(required = false) Currency currency,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        TransactionPageResponse response = cardService.getTransactionHistory(
                cardId, type, transaction_id, external_id, currency, page, size
        );
        return ResponseEntity.ok(response);
    }

}
