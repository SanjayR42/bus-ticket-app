package com.bus.reservation.service;

import com.bus.reservation.model.Booking;
import com.bus.reservation.model.SeatHold;
import com.bus.reservation.repository.BookingRepository;
import com.bus.reservation.repository.SeatHoldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksService {

    private final SeatHoldRepository seatHoldRepository;
    private final BookingRepository bookingRepository;

    /**
     * Clean up expired seat holds every minute
     */
    @Scheduled(fixedRate = 60000) // 1 minute
    @Transactional
    public void cleanupExpiredSeatHolds() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<SeatHold> expiredHolds = seatHoldRepository.findByHoldUntilBefore(now);
            
            if (!expiredHolds.isEmpty()) {
                int count = expiredHolds.size();
                seatHoldRepository.deleteAll(expiredHolds);
                log.info("Cleaned up {} expired seat holds", count);
            }
        } catch (Exception e) {
            log.error("Failed to clean up expired seat holds: {}", e.getMessage());
        }
    }

    /**
     * Auto-cancel unpaid bookings after 30 minutes
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void autoCancelUnpaidBookings() {
        try {
            LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
            
            List<Booking> unpaidBookings = bookingRepository.findByStatusAndBookingDateBefore(
                "PENDING_PAYMENT", thirtyMinutesAgo
            );
            
            int cancelledCount = 0;
            for (Booking booking : unpaidBookings) {
                try {
                    // Free up the seats
                    for (var seat : booking.getSeats()) {
                        seat.setBooked(false);
                    }
                    
                    booking.setStatus("CANCELLED");
                    bookingRepository.save(booking);
                    
                    cancelledCount++;
                    log.info("Auto-cancelled unpaid booking: {}", booking.getId());
                    
                } catch (Exception e) {
                    log.error("Failed to auto-cancel booking {}: {}", booking.getId(), e.getMessage());
                }
            }
            
            if (cancelledCount > 0) {
                log.info("Auto-cancelled {} unpaid bookings", cancelledCount);
            }
            
        } catch (Exception e) {
            log.error("Failed to auto-cancel unpaid bookings: {}", e.getMessage());
        }
    }

    /**
     * Mark completed trips (trips that have departed)
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void markCompletedTrips() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            List<Booking> departedBookings = bookingRepository.findByStatusAndTripDepartureTimeBefore(
                "CONFIRMED", now
            );
            
            int completedCount = 0;
            for (Booking booking : departedBookings) {
                try {
                    booking.setStatus("COMPLETED");
                    bookingRepository.save(booking);
                    completedCount++;
                } catch (Exception e) {
                    log.error("Failed to mark booking {} as completed: {}", booking.getId(), e.getMessage());
                }
            }
            
            if (completedCount > 0) {
                log.info("Marked {} bookings as completed", completedCount);
            }
            
        } catch (Exception e) {
            log.error("Failed to mark completed trips: {}", e.getMessage());
        }
    }

    /**
     * Clean up old completed bookings (older than 30 days)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void cleanupOldBookings() {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            
            List<Booking> oldBookings = bookingRepository.findByStatusAndBookingDateBefore(
                "COMPLETED", thirtyDaysAgo
            );
            
            int archivedCount = 0;
            for (Booking booking : oldBookings) {
                try {
                    // Mark as archived instead of deleting
                    booking.setStatus("ARCHIVED");
                    bookingRepository.save(booking);
                    
                    archivedCount++;
                } catch (Exception e) {
                    log.error("Failed to cleanup booking {}: {}", booking.getId(), e.getMessage());
                }
            }
            
            if (archivedCount > 0) {
                log.info("Archived {} old completed bookings", archivedCount);
            }
            
        } catch (Exception e) {
            log.error("Failed to cleanup old bookings: {}", e.getMessage());
        }
    }

    /**
     * Daily system status report (log only)
     */
    @Scheduled(cron = "0 0 23 * * ?") // Run daily at 11 PM
    @Transactional(readOnly = true)
    public void generateDailySystemReport() {
        try {
            // Get counts for different statuses
            long totalBookings = bookingRepository.count();
            long confirmedBookings = bookingRepository.findByStatus("CONFIRMED").size();
            long completedBookings = bookingRepository.findByStatus("COMPLETED").size();
            long cancelledBookings = bookingRepository.findByStatus("CANCELLED").size();
            
            log.info("📊 Daily System Report:");
            log.info("Total Bookings: {}", totalBookings);
            log.info("Confirmed Bookings: {}", confirmedBookings);
            log.info("Completed Bookings: {}", completedBookings);
            log.info("Cancelled Bookings: {}", cancelledBookings);
            
        } catch (Exception e) {
            log.error("Failed to generate daily system report: {}", e.getMessage());
        }
    }
}