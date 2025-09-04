package com.bus.reservation.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long bookingId;
    private double amount;
    private String method;
    
    // Additional payment details
    private String cardNumber;       // For card payments
    private String cardHolderName;   // For card payments
    private String expiryDate;       // For card payments
    private String cvv;              // For card payments
    private String upiId;            // For UPI payments
    private String walletType;       // For wallet payments
    
    // Customer information for payment gateway
    private String customerEmail;
    private String customerPhone;
}