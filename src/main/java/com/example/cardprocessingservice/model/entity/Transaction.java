package com.example.cardprocessingservice.model.entity;

import com.example.cardprocessingservice.model.base.IdEntity;
import com.example.cardprocessingservice.model.enums.Currency;
import com.example.cardprocessingservice.model.enums.TransactionPurpose;
import com.example.cardprocessingservice.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Transaction extends IdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @Column(nullable = false, unique = true)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionPurpose purpose;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal afterBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    private Long exchangeRate;
}
