package com.bus.reservation.service;

import com.bus.reservation.model.User;
import com.bus.reservation.repository.UserRepository;
import com.bus.reservation.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public User registerUser(String name, String email, String phone, String password, User.Role role) {
        log.info("Registering user: {}", email);
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered!");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new RuntimeException("Phone number already registered!");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        return userRepository.save(user);
    }

    public Optional<User> findUserByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    public Optional<Map<String, Object>> loginUser(String email, String password) {
        log.info("Login attempt for: {}", email);
        
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            if (userOptional.isEmpty()) {
                log.warn("User not found: {}", email);
                return Optional.empty();
            }

            User user = userOptional.get();
            
        
            if (!passwordEncoder.matches(password, user.getPassword())) {
                log.warn("Password mismatch for user: {}", email);
                return Optional.empty();
            }

            // Generate JWT token
            String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
            
        
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("userId", user.getId());
            response.put("name", user.getName());
            response.put("role", user.getRole().name());
            response.put("email", user.getEmail());

            log.info("Login successful for: {}", email);
            return Optional.of(response);
            
        } catch (Exception e) {
            log.error("Login error for {}: {}", email, e.getMessage(), e);
            return Optional.empty();
        }
    }

    // Optional: Method to handle password migration for existing users with plain text passwords
    @Transactional
    public void migratePlainTextPasswords() {
        log.info("Starting password migration for existing users...");
        
        userRepository.findAll().forEach(user -> {
            String currentPassword = user.getPassword();
            
            // Check if password is not encoded (doesn't start with BCrypt pattern)
            if (currentPassword != null && !currentPassword.startsWith("$2a$")) {
                log.info("Migrating password for user: {}", user.getEmail());
                user.setPassword(passwordEncoder.encode(currentPassword));
                userRepository.save(user);
            }
        });
        
        log.info("Password migration completed");
    }
}