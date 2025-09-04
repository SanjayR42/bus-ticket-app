package com.bus.reservation.service;

import com.bus.reservation.model.*;
import com.bus.reservation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final SeatRepository seatRepository;
    
    public Trip getTripById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found with id: " + id));
    }

    // Admin: Schedule new trip
    public Trip scheduleTrip(Long busId, Long routeId, LocalDateTime departureTime,
                             LocalDateTime arrivalTime, Double fare) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new RuntimeException("Bus not found"));
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found"));

        Trip trip = Trip.builder()
                .bus(bus)
                .route(route)
                .departureTime(departureTime)
                .arrivalTime(arrivalTime)
                .fare(fare)
                .build();

        Trip savedTrip = tripRepository.save(trip);

        // Auto-generate seats for this trip
        for (int i = 1; i <= bus.getTotalSeats(); i++) {
            Seat seat = Seat.builder()
                    .trip(savedTrip)
                    .seatNumber("S" + i)
                    .seatType((i % 4 == 1 || i % 4 == 0) ? "Window" : "Aisle")
                    .isBooked(false)
                    .build();
            seatRepository.save(seat);
        }

        return savedTrip;
    }

    // Search trips by source, destination, date
    public List<Trip> searchTrips(String source, String destination, LocalDateTime date) {
        return tripRepository.findByRouteSourceAndRouteDestinationAndDepartureTimeBetween(
                source, destination,
                date.toLocalDate().atStartOfDay(),
                date.toLocalDate().atTime(23, 59, 59)
        );
    }

    // Get seat availability for a trip
    public List<Seat> getTripSeats(Long tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        return seatRepository.findByTripId(trip.getId());
    }
}
