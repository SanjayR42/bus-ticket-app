package com.bus.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResult {
    private boolean success;
    private String paymentId;
    private String transactionId;
    private String status;
    private String message;
    private double amount;
    
    public static PaymentResult success(String paymentId, String transactionId, double amount) {
        return new PaymentResult(true, paymentId, transactionId, "SUCCESS", "Payment successful", amount);
    }
    
    public static PaymentResult failure(String message) {
        return new PaymentResult(false, null, null, "FAILED", message, 0);
    }
    
    public static PaymentResult pending(String paymentId) {
        return new PaymentResult(false, paymentId, null, "PENDING", "Payment pending", 0);
    }
}