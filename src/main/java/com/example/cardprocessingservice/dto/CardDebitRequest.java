package com.example.cardprocessingservice.dto;

import com.example.cardprocessingservice.model.enums.Currency;
import com.example.cardprocessingservice.model.enums.TransactionPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardDebitRequest {

    @NotBlank(message = "external_id is required")
    private String external_id;

    @NotNull(message = "amount is required")
    private long amount;

    private Currency currency = Currency.UZS;

    @NotNull(message = "purpose is required")
    private TransactionPurpose purpose;
}
