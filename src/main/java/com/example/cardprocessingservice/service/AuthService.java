package com.example.cardprocessingservice.service;

import com.example.cardprocessingservice.config.CustomUserDetails;
import com.example.cardprocessingservice.config.JwtUtil;
import com.example.cardprocessingservice.model.entity.AuthUser;
import com.example.cardprocessingservice.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final AuthUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return jwtTokenProvider.generateToken(userDetails.getUsername());
    }

    public AuthUser register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }

        AuthUser user = new AuthUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }
}
