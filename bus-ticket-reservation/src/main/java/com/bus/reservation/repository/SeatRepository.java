package com.bus.reservation.repository;

import com.bus.reservation.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    List<Seat> findByTripId(Long tripId);
    
    //concurrency control
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.id = :id")

    Optional<Seat> findByIdWithLock(Long id);
    

    @Query("SELECT s FROM Seat s WHERE s.trip.id = :tripId AND s.isBooked = false")
    List<Seat> findAvailableSeatsByTripId(@Param("tripId") Long tripId);
    
    @Query("SELECT s FROM Seat s WHERE s.trip.id = :tripId AND s.isBooked = true")
    List<Seat> findBookedSeatsByTripId(@Param("tripId") Long tripId);
    
    long countByTripIdAndIsBooked(Long tripId, boolean isBooked);
}