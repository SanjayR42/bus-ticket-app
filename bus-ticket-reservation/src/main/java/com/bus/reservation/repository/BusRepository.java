package com.bus.reservation.repository;

import com.bus.reservation.model.Bus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusRepository extends JpaRepository<Bus, Long> {
    
}