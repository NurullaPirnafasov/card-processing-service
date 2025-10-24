package com.example.cardprocessingservice.controller;

import com.example.cardprocessingservice.dto.CardCreateRequest;
import com.example.cardprocessingservice.dto.CardResponse;
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

    @PostMapping
    public ResponseEntity<CardResponse> createCard(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CardCreateRequest request
    ) {
        CardResponse response = cardService.createCard(request, idempotencyKey);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
