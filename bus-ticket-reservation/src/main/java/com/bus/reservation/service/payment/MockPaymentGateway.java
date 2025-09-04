package com.bus.reservation.service.payment;

import com.bus.reservation.dto.PaymentRequest;
import com.bus.reservation.dto.PaymentResult;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Profile("!prod") // Use only in non-production environments
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public PaymentResult processPayment(PaymentRequest request) {
        // Simulate payment processing with 90% success rate
        boolean success = Math.random() > 0.1;
        
        if (success) {
            return PaymentResult.success(
                "pay_" + UUID.randomUUID().toString().substring(0, 8),
                "txn_" + UUID.randomUUID().toString().substring(0, 8),
                request.getAmount()
            );
        } else {
            return PaymentResult.failure("Payment declined by bank");
        }
    }

    @Override
    public PaymentResult verifyPayment(String paymentId) {
        // Mock verification - always return success for existing payments
        return PaymentResult.success(paymentId, "verify_txn_" + UUID.randomUUID(), 0);
    }

    @Override
    public PaymentResult refundPayment(String paymentId, double amount) {
        // Mock refund - always successful
        return PaymentResult.success(
            "refund_" + UUID.randomUUID().toString().substring(0, 8),
            "refund_txn_" + UUID.randomUUID().toString().substring(0, 8),
            amount
        );
    }
}