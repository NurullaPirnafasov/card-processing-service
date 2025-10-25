package com.example.cardprocessingservice.dto;

import com.example.cardprocessingservice.model.enums.Currency;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardCreditRequest {
    private String external_id;
    private long amount;
    private Currency currency = Currency.UZS;
}
