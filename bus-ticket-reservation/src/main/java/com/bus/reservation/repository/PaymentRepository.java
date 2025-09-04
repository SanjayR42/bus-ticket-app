package com.bus.reservation.repository;

import com.bus.reservation.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByBookingId(Long bookingId);
    
    @Query("SELECT p FROM Payment p WHERE p.booking.user.id = :userId")
    List<Payment> findByBookingUserId(@Param("userId") Long userId);
    
    List<Payment> findByStatus(String status);
    List<Payment> findByPaymentMethod(String paymentMethod);
}