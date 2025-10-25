package com.example.cardprocessingservice.model.entity;

import com.example.cardprocessingservice.model.base.IdEntity;
import com.example.cardprocessingservice.model.enums.CardStatus;
import com.example.cardprocessingservice.model.enums.Currency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Card extends IdEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AuthUser authUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status = CardStatus.ACTIVE;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency = Currency.UZS;

    @Version
    private Long version;
}
