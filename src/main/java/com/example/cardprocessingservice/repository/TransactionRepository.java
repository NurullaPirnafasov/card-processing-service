package com.example.cardprocessingservice.repository;

import com.example.cardprocessingservice.model.entity.Card;
import com.example.cardprocessingservice.model.entity.Transaction;
import com.example.cardprocessingservice.model.enums.Currency;
import com.example.cardprocessingservice.model.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query("""
        select t from Transaction t 
        where t.card = :card
        and (:type is null or t.type = :type)
        and (:transactionId is null or t.id = :transactionId)
        and (:externalId is null or t.externalId = :externalId)
        and (:currency is null or t.currency = :currency)
        """)
    Page<Transaction> findAllByFilters(@Param("card") Card card,
                                       @Param("type") TransactionType type,
                                       @Param("transactionId") String transactionId,
                                       @Param("externalId") String externalId,
                                       @Param("currency") Currency currency,
                                       Pageable pageable);
}
