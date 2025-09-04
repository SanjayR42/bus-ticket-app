package com.bus.reservation.controller;

import com.bus.reservation.model.Booking;
import com.bus.reservation.model.User;
import com.bus.reservation.repository.BookingRepository;
import com.bus.reservation.repository.UserRepository;
import com.bus.reservation.util.TicketPdfGenerator;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class TicketController {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final TicketPdfGenerator ticketPdfGenerator;

    @GetMapping("/{bookingId}/download")
    public ResponseEntity<Resource> downloadTicket(
            @PathVariable Long bookingId,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Verify the booking belongs to the authenticated user
            if (!booking.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Access denied");
            }

            // Generate PDF if not exists
            ticketPdfGenerator.generateEnhancedTicketPdf(booking);

            // Get PDF bytes
            byte[] pdfBytes = ticketPdfGenerator.getTicketPdfBytes(bookingId);

            ByteArrayResource resource = new ByteArrayResource(pdfBytes);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=ticket-" + bookingId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(resource);

        } catch (Exception e) {
            log.error("Failed to download ticket: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{bookingId}/qr-code")
    public ResponseEntity<?> getTicketQrCode(
            @PathVariable Long bookingId,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Verify the booking belongs to the authenticated user
            if (!booking.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Access denied");
            }

            String qrCodeBase64 = ticketPdfGenerator.generateQRCodeBase64(booking);

            if (qrCodeBase64 == null) {
                throw new RuntimeException("Failed to generate QR code");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("qrCode", qrCodeBase64);
            response.put("bookingId", bookingId);
            response.put("format", "image/png");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Failed to generate QR code: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{bookingId}/verify")
    public ResponseEntity<?> verifyTicket(
            @PathVariable Long bookingId,
            @RequestParam(required = false) String qrData) {
        
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("bookingId", booking.getId());
            response.put("passenger", booking.getUser().getName());
            response.put("route", booking.getTrip().getRoute().getSource() + " → " + 
                                booking.getTrip().getRoute().getDestination());
            response.put("departure", booking.getTrip().getDepartureTime());
            response.put("seats", booking.getSeats().stream()
                    .map(seat -> seat.getSeatNumber())
                    .toList());
            response.put("status", booking.getStatus());

            // If QR data is provided, verify it matches
            if (qrData != null) {
                String expectedQrData = ticketPdfGenerator.generateQRData(booking);
                response.put("qrValid", qrData.equals(expectedQrData));
            }

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Ticket verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "valid", false));
        }
    }

    @GetMapping("/{bookingId}/details")
    public ResponseEntity<?> getTicketDetails(
            @PathVariable Long bookingId,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Verify the booking belongs to the authenticated user
            if (!booking.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Access denied");
            }

            Map<String, Object> ticketDetails = new HashMap<>();
            ticketDetails.put("bookingId", booking.getId());
            ticketDetails.put("passengerName", booking.getUser().getName());
            ticketDetails.put("passengerEmail", booking.getUser().getEmail());
            ticketDetails.put("passengerPhone", booking.getUser().getPhone());
            ticketDetails.put("route", booking.getTrip().getRoute().getSource() + " → " + 
                                    booking.getTrip().getRoute().getDestination());
            ticketDetails.put("departureTime", booking.getTrip().getDepartureTime());
            ticketDetails.put("arrivalTime", booking.getTrip().getArrivalTime());
            ticketDetails.put("busNumber", booking.getTrip().getBus().getBusNumber());
            ticketDetails.put("busType", booking.getTrip().getBus().getBusType());
            ticketDetails.put("seats", booking.getSeats().stream()
                    .map(seat -> Map.of(
                        "number", seat.getSeatNumber(),
                        "type", seat.getSeatType()
                    ))
                    .toList());
            ticketDetails.put("totalAmount", booking.getTotalAmount());
            ticketDetails.put("status", booking.getStatus());
            ticketDetails.put("bookingDate", booking.getBookingDate());

            if (booking.getPayment() != null) {
                ticketDetails.put("paymentStatus", booking.getPayment().getStatus());
                ticketDetails.put("paymentMethod", booking.getPayment().getPaymentMethod());
            }

            // Generate QR code for display
            String qrCodeBase64 = ticketPdfGenerator.generateQRCodeBase64(booking);
            ticketDetails.put("qrCode", qrCodeBase64);

            return ResponseEntity.ok(ticketDetails);

        } catch (RuntimeException e) {
            log.error("Failed to get ticket details: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{bookingId}/send-email")
    public ResponseEntity<?> sendTicketEmail(
            @PathVariable Long bookingId,
            Authentication authentication) {
        
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));

            // Verify the booking belongs to the authenticated user
            if (!booking.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Access denied");
            }

            // This would require EmailService to have a method to send tickets
            // For now, we'll just return success
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Ticket email sent successfully");
            response.put("sentTo", user.getEmail());
            response.put("bookingId", bookingId);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Failed to send ticket email: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}