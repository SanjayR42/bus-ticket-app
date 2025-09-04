package com.bus.reservation.controller;

import com.bus.reservation.model.Booking;
import com.bus.reservation.model.User;
import com.bus.reservation.repository.UserRepository;
import com.bus.reservation.service.BookingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    @PostMapping("/hold")
    public ResponseEntity<?> holdSeats(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {

        Long tripId = Long.valueOf(request.get("tripId").toString());
        List<Integer> seatIdsRaw = (List<Integer>) request.get("seatIds");
        List<Long> seatIds = seatIdsRaw.stream().map(Long::valueOf).toList();

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            String sessionId = bookingService.holdSeats(tripId, seatIds, user.getId());
            return ResponseEntity.ok(Map.of("sessionId", sessionId, "message", "Seats held successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmBooking(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {

        String sessionId = request.get("sessionId").toString();
        String paymentMethod = request.get("paymentMethod").toString();

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            Booking booking = bookingService.confirmBooking(sessionId, user.getId(), paymentMethod);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createBooking(
            Authentication authentication,
            @RequestBody Map<String, Object> request) {

        Long tripId = Long.valueOf(request.get("tripId").toString());
        List<Integer> seatIdsRaw = (List<Integer>) request.get("seatIds");
        List<Long> seatIds = seatIdsRaw.stream().map(Long::valueOf).toList();
        String paymentMethod = request.get("paymentMethod").toString();

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            String sessionId = bookingService.holdSeats(tripId, seatIds, user.getId());
            Booking booking = bookingService.confirmBooking(sessionId, user.getId(), paymentMethod);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<List<Booking>> getMyBookings(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(bookingService.getUserBookings(user.getId()));
    }
    
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<?> cancelBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            bookingService.cancelBooking(bookingId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Booking cancelled successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingDetails(
            @PathVariable Long bookingId,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // This method needs to be implemented in BookingService
            Booking booking = bookingService.getBookingById(bookingId);
            return ResponseEntity.ok(booking);
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}