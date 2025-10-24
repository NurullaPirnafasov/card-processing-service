package com.example.cardprocessingservice.dto;

import com.example.cardprocessingservice.model.enums.CardStatus;
import com.example.cardprocessingservice.model.enums.Currency;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardResponse {

    private String card_id;
    private Long user_id;
    private CardStatus status;
    private Long balance;
    private Currency currency;
}
