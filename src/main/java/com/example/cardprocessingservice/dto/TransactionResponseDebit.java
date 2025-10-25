package com.example.cardprocessingservice.dto;

import com.example.cardprocessingservice.model.enums.Currency;
import com.example.cardprocessingservice.model.enums.TransactionPurpose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionResponseDebit {

    private String transaction_id;
    private String external_id;
    private String card_id;
    private long amount;
    private long after_balance;
    private Currency currency;
    private TransactionPurpose purpose;
    private long exchange_rate;
}
