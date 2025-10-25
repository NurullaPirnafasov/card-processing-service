package com.example.cardprocessingservice.repository;

import com.example.cardprocessingservice.model.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
