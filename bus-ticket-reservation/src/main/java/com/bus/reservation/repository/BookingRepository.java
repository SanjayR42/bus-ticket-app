package com.bus.reservation.repository;

import com.bus.reservation.model.Booking;
import com.bus.reservation.model.SeatHold;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    
List<Booking> findByStatusAndBookingDateBefore(String status, LocalDateTime date);
    
    List<Booking> findByStatusAndTripDepartureTimeBefore(String status, LocalDateTime date);
    
    List<Booking> findByStatus(String status);
    
    // Seat hold method
    List<SeatHold> findByHoldUntilBefore(LocalDateTime date);
}
