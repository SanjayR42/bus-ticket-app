package com.bus.reservation.repository;

import com.bus.reservation.model.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {
    
    List<SeatHold> findBySessionId(String sessionId);
    
    Optional<SeatHold> findBySeatIdAndSessionId(Long seatId, String sessionId);
    
    List<SeatHold> findByHoldUntilBefore(LocalDateTime timestamp);
    
    @Query("SELECT sh FROM SeatHold sh WHERE sh.seat.id = :seatId AND sh.holdUntil > :now")
    List<SeatHold> findActiveHoldsForSeat(@Param("seatId") Long seatId, @Param("now") LocalDateTime now);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM SeatHold sh WHERE sh.holdUntil < :now")
    void deleteExpiredHolds(@Param("now") LocalDateTime now);
    
    @Transactional
    @Modifying
    void deleteBySessionId(String sessionId);
}