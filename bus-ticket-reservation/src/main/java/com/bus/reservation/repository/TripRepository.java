package com.bus.reservation.repository;

import com.bus.reservation.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {
    
	Optional<Trip> findById(Long id);
	
    @Query("SELECT t FROM Trip t WHERE t.route.source = :source AND t.route.destination = :destination AND t.departureTime BETWEEN :start AND :end")
    List<Trip> findByRouteSourceAndRouteDestinationAndDepartureTimeBetween(
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    List<Trip> findByBusId(Long busId);
    List<Trip> findByRouteId(Long routeId);
}