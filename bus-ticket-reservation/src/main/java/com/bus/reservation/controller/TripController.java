package com.bus.reservation.controller;

import com.bus.reservation.model.Seat;
import com.bus.reservation.model.Trip;
import com.bus.reservation.repository.TripRepository; // ADD THIS IMPORT
import com.bus.reservation.service.TripService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TripController {

    private final TripService tripService;
    private final TripRepository tripRepository; // ADD THIS FIELD
    
    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripById(@PathVariable Long id) {
        Trip trip = tripService.getTripById(id);
        return ResponseEntity.ok(trip);
    }

    // Admin
    @PostMapping
    public ResponseEntity<Trip> createTrip( // CHANGED METHOD NAME
            @RequestParam Long busId,
            @RequestParam Long routeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime arrivalTime,
            @RequestParam Double fare) {
        return ResponseEntity.ok(
                tripService.scheduleTrip(busId, routeId, departureTime, arrivalTime, fare)
        );
    }

    // Search trips by source, destination, date
    @GetMapping("/search")
    public ResponseEntity<List<Trip>> searchTrips(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return ResponseEntity.ok(tripService.searchTrips(source, destination, date));
    }

    // Get seat availability for trip
    @GetMapping("/{tripId}/seats")
    public ResponseEntity<List<Seat>> getTripSeats(@PathVariable Long tripId) {
        return ResponseEntity.ok(tripService.getTripSeats(tripId));
    }
    
    // Get all trips
    @GetMapping
    public ResponseEntity<List<Trip>> getAllTrips() {
        return ResponseEntity.ok(tripRepository.findAll());
    }

    // Update trip
    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable Long id, @RequestBody Trip tripDetails) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        
        // Update trip details
        trip.setBus(tripDetails.getBus());
        trip.setRoute(tripDetails.getRoute());
        trip.setDepartureTime(tripDetails.getDepartureTime());
        trip.setArrivalTime(tripDetails.getArrivalTime());
        trip.setFare(tripDetails.getFare());
        
        return ResponseEntity.ok(tripRepository.save(trip));
    }

    // Delete trip
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrip(@PathVariable Long id) {
        if (!tripRepository.existsById(id)) {
            throw new RuntimeException("Trip not found with id: " + id);
        }
        tripRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}