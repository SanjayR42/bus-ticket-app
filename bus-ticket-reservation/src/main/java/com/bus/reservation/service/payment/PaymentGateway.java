package com.bus.reservation.service.payment;

import com.bus.reservation.dto.PaymentRequest;
import com.bus.reservation.dto.PaymentResult;

public interface PaymentGateway {
    PaymentResult processPayment(PaymentRequest request);
    PaymentResult verifyPayment(String paymentId);
    PaymentResult refundPayment(String paymentId, double amount);
}