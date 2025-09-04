package com.bus.reservation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private String paymentMethod; // CARD, UPI, NETBANKING, WALLET

    private String status;       // PENDING, SUCCESS, FAILED, REFUNDED

    private String paymentGatewayId; // External payment gateway ID

    private String transactionId;    // External transaction ID

    private String gatewayResponse;  // Raw response from payment gateway

    @Builder.Default
    private LocalDateTime paymentDate = LocalDateTime.now();

    private LocalDateTime refundDate;

    @OneToOne
    @JoinColumn(name = "booking_id", nullable = false)
    @JsonIgnore
    private Booking booking;
    
    public User getUser() {
        return booking != null ? booking.getUser() : null;
    }
}