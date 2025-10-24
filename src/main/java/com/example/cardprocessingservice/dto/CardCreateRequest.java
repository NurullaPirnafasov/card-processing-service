package com.example.cardprocessingservice.dto;

import com.example.cardprocessingservice.model.enums.CardStatus;
import com.example.cardprocessingservice.model.enums.Currency;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardCreateRequest {

    @NotNull(message = "User must be selected")
    private Long user_id;

    private CardStatus status = CardStatus.ACTIVE;

    @Max(value = 10000, message = "Initial amount cannot be more than 10,000")
    private Long initial_amount = 0L;

    private Currency currency = Currency.UZS;
}
