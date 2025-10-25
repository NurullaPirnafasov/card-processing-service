package com.example.cardprocessingservice.mapper;

import com.example.cardprocessingservice.dto.TransactionResponseCredit;
import com.example.cardprocessingservice.dto.TransactionResponseDebit;
import com.example.cardprocessingservice.model.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "transaction_id", source = "id")
    @Mapping(target = "external_id", source = "externalId")
    @Mapping(target = "card_id", source = "card.id")
    @Mapping(target = "amount", expression = "java(tx.getAmount().longValue())")
    @Mapping(target = "after_balance", expression = "java(tx.getAfterBalance().longValue())")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "purpose", source = "purpose")
    @Mapping(target = "exchange_rate", source = "exchangeRate")
    TransactionResponseDebit toResponseDebit(Transaction tx);

    @Mapping(target = "transaction_id", source = "id")
    @Mapping(target = "external_id", source = "externalId")
    @Mapping(target = "card_id", source = "card.id")
    @Mapping(target = "amount", expression = "java(tx.getAmount().longValue())")
    @Mapping(target = "after_balance", expression = "java(tx.getAfterBalance().longValue())")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "exchange_rate", source = "exchangeRate")
    TransactionResponseCredit toResponseCredit(Transaction tx);
}
