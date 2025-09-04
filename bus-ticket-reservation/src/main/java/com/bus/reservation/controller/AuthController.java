package com.bus.reservation.controller;

import com.bus.reservation.dto.LoginRequest;
import com.bus.reservation.dto.RegisterRequest;
import com.bus.reservation.model.User;
import com.bus.reservation.security.JwtUtils;
import com.bus.reservation.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User newUser = authService.registerUser(
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getPassword(),
                User.Role.valueOf(request.getRole().toUpperCase())
            );

            return authService.loginUser(newUser.getEmail(), request.getPassword())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(500)
                    .body(Map.of("error", "Registration successful but login failed")));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid role specified"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            System.out.println("Login request received for: " + request.getEmail());
            
            Optional<Map<String, Object>> authResponse = authService.loginUser(
                request.getEmail(), 
                request.getPassword()
            );
            
            if (authResponse.isPresent()) {
                System.out.println("Login successful for: " + request.getEmail());
                return ResponseEntity.ok(authResponse.get());
            } else {
                System.out.println("Login failed - invalid credentials for: " + request.getEmail());
                return ResponseEntity.status(401)
                    .body(Map.of("error", "Invalid credentials"));
            }
        } catch (Exception e) {
            System.err.println("Login error for " + request.getEmail() + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                .body(Map.of("error", "Internal server error during login: " + e.getMessage()));
        }
    }
    
   
    

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> tokenRequest) {
        try {
            String token = tokenRequest.get("token");
            
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token is required"));
            }
            
            boolean isValid = jwtUtils.isTokenValid(token, jwtUtils.extractUsername(token));
            
            return ResponseEntity.ok(Map.of("valid", isValid));
            
        } catch (Exception e) {
            System.err.println("Token validation error: " + e.getMessage());
            return ResponseEntity.status(500)
                .body(Map.of("error", "Token validation failed"));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        
        
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
}