package com.bus.reservation.service;

import com.bus.reservation.model.Bus;
import com.bus.reservation.model.Trip;
import com.bus.reservation.repository.BusRepository;
import com.bus.reservation.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BusService {

    private final BusRepository busRepository;
    private final TripRepository tripRepository;

    public Bus addBus(Bus bus) {
        return busRepository.save(bus);
    }

    public List<Bus> getAllBuses() {
        try {
            return busRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch buses: " + e.getMessage());
        }
    }

    // Fixed search method - should search trips, not buses directly
    public List<Trip> searchBuses(String source, String destination) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        
        return tripRepository.findByRouteSourceAndRouteDestinationAndDepartureTimeBetween(
                source,
                destination,
                now,
                tomorrow
        );
    }

    // Add more specific search methods
    public List<Trip> searchBusesWithDate(String source, String destination, LocalDateTime date) {
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(23, 59, 59);
        
        return tripRepository.findByRouteSourceAndRouteDestinationAndDepartureTimeBetween(
                source,
                destination,
                startOfDay,
                endOfDay
        );
    }
}