package com.bus.reservation.service;

import com.bus.reservation.dto.PaymentRequest;
import com.bus.reservation.dto.PaymentResult;
import com.bus.reservation.model.Booking;
import com.bus.reservation.model.Payment;
import com.bus.reservation.repository.BookingRepository;
import com.bus.reservation.repository.PaymentRepository;
import com.bus.reservation.service.payment.PaymentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

    @Transactional
    public Payment processPayment(PaymentRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Set customer information if not provided
        if (request.getCustomerEmail() == null) {
            request.setCustomerEmail(booking.getUser().getEmail());
        }
        if (request.getCustomerPhone() == null) {
            request.setCustomerPhone(booking.getUser().getPhone());
        }

        // Process payment through gateway
        PaymentResult result = paymentGateway.processPayment(request);

        // Create payment record
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(request.getAmount())
                .paymentMethod(request.getMethod())
                .status(result.getStatus())
                .paymentGatewayId(result.getPaymentId())
                .transactionId(result.getTransactionId())
                .gatewayResponse(result.getMessage())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Update booking status based on payment result
        if ("SUCCESS".equals(result.getStatus())) {
            booking.setStatus("CONFIRMED");
            log.info("Payment successful for booking {}: {}", request.getBookingId(), result.getMessage());
        } else if ("PENDING".equals(result.getStatus())) {
            booking.setStatus("PENDING_PAYMENT");
            log.warn("Payment pending for booking {}: {}", request.getBookingId(), result.getMessage());
        } else {
            booking.setStatus("PAYMENT_FAILED");
            log.error("Payment failed for booking {}: {}", request.getBookingId(), result.getMessage());
        }

        bookingRepository.save(booking);

        return savedPayment;
    }

    @Transactional
    public Payment retryPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!"FAILED".equals(payment.getStatus())) {
            throw new RuntimeException("Only failed payments can be retried");
        }

        Booking booking = payment.getBooking();

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setBookingId(booking.getId());
        paymentRequest.setAmount(payment.getAmount());
        paymentRequest.setMethod(payment.getPaymentMethod());
        paymentRequest.setCustomerEmail(booking.getUser().getEmail());
        paymentRequest.setCustomerPhone(booking.getUser().getPhone());

        PaymentResult result = paymentGateway.processPayment(paymentRequest);

        // Update payment record
        payment.setStatus(result.getStatus());
        payment.setPaymentGatewayId(result.getPaymentId());
        payment.setTransactionId(result.getTransactionId());
        payment.setGatewayResponse(result.getMessage());

        Payment updatedPayment = paymentRepository.save(payment);

        // Update booking status
        if ("SUCCESS".equals(result.getStatus())) {
            booking.setStatus("CONFIRMED");
        } else if ("PENDING".equals(result.getStatus())) {
            booking.setStatus("PENDING_PAYMENT");
        }

        bookingRepository.save(booking);

        return updatedPayment;
    }

    @Transactional
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (!"SUCCESS".equals(payment.getStatus())) {
            throw new RuntimeException("Only successful payments can be refunded");
        }

        Booking booking = payment.getBooking();

        // Process refund through gateway
        PaymentResult result = paymentGateway.refundPayment(
                payment.getPaymentGatewayId(), 
                payment.getAmount()
        );

        if (result.isSuccess()) {
            payment.setStatus("REFUNDED");
            payment.setRefundDate(java.time.LocalDateTime.now());
            payment.setGatewayResponse("Refund processed: " + result.getMessage());
            
            // Update booking status
            booking.setStatus("REFUNDED");
            bookingRepository.save(booking);
            
            log.info("Refund successful for payment {}: {}", paymentId, result.getMessage());
        } else {
            payment.setGatewayResponse("Refund failed: " + result.getMessage());
            log.error("Refund failed for payment {}: {}", paymentId, result.getMessage());
        }

        return paymentRepository.save(payment);
    }

    public Payment getPaymentByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found for booking"));
    }

    // Keep the original method for backward compatibility
    @Transactional
    public Payment processPayment(Long bookingId, double amount, String method) {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(bookingId);
        request.setAmount(amount);
        request.setMethod(method);
        return processPayment(request);
    }
}