package com.example.cardprocessingservice.repository;

import com.example.cardprocessingservice.model.entity.Card;
import com.example.cardprocessingservice.model.entity.AuthUser;
import com.example.cardprocessingservice.model.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, String> {
    List<Card> findByAuthUserAndStatusNot(AuthUser authUser, CardStatus status);
}
