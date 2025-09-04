package com.bus.reservation.service;

import com.bus.reservation.model.*;
import com.bus.reservation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional

public class BookingService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final SeatRepository seatRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SeatHoldRepository seatHoldRepository;

    private static final int SEAT_HOLD_DURATION_MINUTES = 10;

    @Transactional
    public String holdSeats(Long tripId, List<Long> seatIds, Long userId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String sessionId = UUID.randomUUID().toString();
        LocalDateTime holdUntil = LocalDateTime.now().plusMinutes(SEAT_HOLD_DURATION_MINUTES);

        for (Long seatId : seatIds) {
            Seat seat = seatRepository.findById(seatId)
                    .orElseThrow(() -> new RuntimeException("Seat not found: " + seatId));

            if (seat.isBooked()) {
                throw new RuntimeException("Seat already booked: " + seat.getSeatNumber());
            }

            List<SeatHold> activeHolds = seatHoldRepository.findActiveHoldsForSeat(seatId, LocalDateTime.now());
            if (!activeHolds.isEmpty()) {
                throw new RuntimeException("Seat is currently held: " + seat.getSeatNumber());
            }

            SeatHold seatHold = SeatHold.builder()
                    .seat(seat)
                    .sessionId(sessionId)
                    .holdUntil(holdUntil)
                    .build();

            seatHoldRepository.save(seatHold);
        }

        return sessionId;
    }

    @Transactional
    public Booking confirmBooking(String sessionId, Long userId, String paymentMethod) {
        List<SeatHold> seatHolds = seatHoldRepository.findBySessionId(sessionId);
        
        if (seatHolds.isEmpty()) {
            throw new RuntimeException("No seats held for session: " + sessionId);
        }

        for (SeatHold hold : seatHolds) {
            if (hold.getHoldUntil().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Seat hold expired for seat: " + hold.getSeat().getSeatNumber());
            }
        }

        Trip trip = seatHolds.get(0).getSeat().getTrip();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        double totalAmount = seatHolds.size() * trip.getFare();

        for (SeatHold hold : seatHolds) {
            Seat seat = hold.getSeat();
            seat.setBooked(true);
            seatRepository.save(seat);
        }

        List<Seat> seats = seatHolds.stream()
                .map(SeatHold::getSeat)
                .toList();

        Booking booking = Booking.builder()
                .user(user)
                .trip(trip)
                .seats(seats)
                .totalAmount(totalAmount)
                .status("CONFIRMED")
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        Payment payment = Payment.builder()
                .booking(savedBooking)
                .amount(totalAmount)
                .paymentMethod(paymentMethod)
                .status("SUCCESS")
                .build();

        paymentRepository.save(payment);
        seatHoldRepository.deleteBySessionId(sessionId);

        return savedBooking;
    }

    @Transactional
    public void releaseSeatHold(String sessionId) {
        seatHoldRepository.deleteBySessionId(sessionId);
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredHolds() {
        LocalDateTime now = LocalDateTime.now();
        List<SeatHold> expiredHolds = seatHoldRepository.findByHoldUntilBefore(now);
        
        if (!expiredHolds.isEmpty()) {
            seatHoldRepository.deleteAll(expiredHolds);
            log.info("Cleaned up {} expired seat holds", expiredHolds.size());
        }
    }

    @Transactional(readOnly = true)
    public List<Seat> getAvailableSeats(Long tripId) {
        return seatRepository.findAvailableSeatsByTripId(tripId);
    }

    @Transactional(readOnly = true)
    public List<SeatHold> getActiveHoldsForSeat(Long seatId) {
        return seatHoldRepository.findActiveHoldsForSeat(seatId, LocalDateTime.now());
    }

    // ADD THESE MISSING METHODS
    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only cancel your own bookings");
        }
        
        if (booking.getStatus().equals("CANCELLED")) {
            throw new RuntimeException("Booking is already cancelled");
        }
        
        if (booking.getStatus().equals("COMPLETED")) {
            throw new RuntimeException("Cannot cancel completed booking");
        }
        
        LocalDateTime departureTime = booking.getTrip().getDepartureTime();
        if (LocalDateTime.now().isAfter(departureTime.minusHours(2))) {
            throw new RuntimeException("Cannot cancel booking within 2 hours of departure");
        }
        
        for (Seat seat : booking.getSeats()) {
            seat.setBooked(false);
            seatRepository.save(seat);
        }
        
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        
        Payment payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatus("REFUNDED");
        paymentRepository.save(payment);
    }

    // ADD THIS MISSING METHOD - SIMPLIFIED VERSION
    @Transactional(readOnly = true)
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
    }

    // OPTIONAL: Add user validation version if needed
    @Transactional(readOnly = true)
    public Booking getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        if (!booking.getUser().getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        
        return booking;
    }
}