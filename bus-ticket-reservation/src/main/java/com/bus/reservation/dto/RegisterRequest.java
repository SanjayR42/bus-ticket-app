package com.bus.reservation.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String phone;
    private String password;
    private String role; // "ADMIN" or "CUSTOMER"
}