package com.example.cardprocessingservice.repository;

import com.example.cardprocessingservice.model.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
}
