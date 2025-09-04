package com.bus.reservation.service;

import com.bus.reservation.model.Booking;
import com.bus.reservation.model.Seat;
import com.bus.reservation.repository.BookingRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final BookingRepository bookingRepository;

    private static final String TICKETS_DIR = "tickets/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    /**
     * Generate enhanced PDF ticket with QR code
     */
    public String generateTicketPdf(Booking booking) {
        try {
            // Create tickets directory if it doesn't exist
            Path ticketsPath = Paths.get(TICKETS_DIR);
            if (!Files.exists(ticketsPath)) {
                Files.createDirectories(ticketsPath);
            }

            String fileName = "ticket-" + booking.getId() + ".pdf";
            Path filePath = ticketsPath.resolve(fileName);

            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    // Set up fonts
                    PDType1Font titleFont = PDType1Font.HELVETICA_BOLD;
                    PDType1Font headerFont = PDType1Font.HELVETICA_BOLD;
                    PDType1Font normalFont = PDType1Font.HELVETICA;

                    // Add header
                    contentStream.setFont(titleFont, 20);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 750);
                    contentStream.showText("BUS TICKET");
                    contentStream.endText();

                    // Draw separator line
                    contentStream.moveTo(50, 730);
                    contentStream.lineTo(550, 730);
                    contentStream.stroke();

                    // Add booking details
                    contentStream.setFont(headerFont, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 700);
                    contentStream.showText("BOOKING DETAILS");
                    contentStream.endText();

                    contentStream.setFont(normalFont, 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 680);
                    contentStream.showText("Booking ID: #" + booking.getId());
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Booking Date: " + booking.getBookingDate().format(DATETIME_FORMATTER));
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Status: " + booking.getStatus());
                    contentStream.endText();

                    // Add passenger details
                    contentStream.setFont(headerFont, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 620);
                    contentStream.showText("PASSENGER INFORMATION");
                    contentStream.endText();

                    contentStream.setFont(normalFont, 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 600);
                    contentStream.showText("Name: " + booking.getUser().getName());
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Email: " + booking.getUser().getEmail());
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Phone: " + booking.getUser().getPhone());
                    contentStream.endText();

                    // Add trip details
                    contentStream.setFont(headerFont, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 540);
                    contentStream.showText("TRIP INFORMATION");
                    contentStream.endText();

                    contentStream.setFont(normalFont, 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 520);
                    contentStream.showText("Route: " + booking.getTrip().getRoute().getSource() + 
                                          " → " + booking.getTrip().getRoute().getDestination());
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Departure: " + 
                                          booking.getTrip().getDepartureTime().format(DATETIME_FORMATTER));
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Arrival: " + 
                                          booking.getTrip().getArrivalTime().format(DATETIME_FORMATTER));
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Bus: " + booking.getTrip().getBus().getBusNumber() + 
                                          " (" + booking.getTrip().getBus().getBusType() + ")");
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Operator: " + booking.getTrip().getBus().getOperatorName());
                    contentStream.endText();

                    // Add seat details
                    contentStream.setFont(headerFont, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 420);
                    contentStream.showText("SEAT INFORMATION");
                    contentStream.endText();

                    contentStream.setFont(normalFont, 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 400);
                    
                    String seats = booking.getSeats().stream()
                            .map(Seat::getSeatNumber)
                            .reduce((s1, s2) -> s1 + ", " + s2)
                            .orElse("No seats");
                    
                    contentStream.showText("Seats: " + seats);
                    contentStream.newLineAtOffset(0, -15);
                    
                    String seatTypes = booking.getSeats().stream()
                            .map(Seat::getSeatType)
                            .distinct()
                            .reduce((s1, s2) -> s1 + ", " + s2)
                            .orElse("");
                    
                    contentStream.showText("Seat Types: " + seatTypes);
                    contentStream.newLineAtOffset(0, -15);
                    contentStream.showText("Total Seats: " + booking.getSeats().size());
                    contentStream.endText();

                    // Add payment details
                    contentStream.setFont(headerFont, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 340);
                    contentStream.showText("PAYMENT INFORMATION");
                    contentStream.endText();

                    contentStream.setFont(normalFont, 10);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 320);
                    contentStream.showText("Total Amount: ₹" + booking.getTotalAmount());
                    
                    if (booking.getPayment() != null) {
                        contentStream.newLineAtOffset(0, -15);
                        contentStream.showText("Payment Method: " + booking.getPayment().getPaymentMethod());
                        contentStream.newLineAtOffset(0, -15);
                        contentStream.showText("Payment Status: " + booking.getPayment().getStatus());
                    }
                    contentStream.endText();

                    // Generate and add QR code
                    String qrData = generateQRData(booking);
                    BufferedImage qrImage = generateQRCodeImage(qrData, 200, 200);
                    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(qrImage, "PNG", baos);
                    byte[] qrImageBytes = baos.toByteArray();
                    
                    PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, qrImageBytes, "qr-code");
                    contentStream.drawImage(pdImage, 400, 600, 100, 100);

                    // Add QR code label
                    contentStream.setFont(normalFont, 8);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(400, 590);
                    contentStream.showText("Scan this QR code for verification");
                    contentStream.endText();

                    // Add footer
                    contentStream.setFont(normalFont, 8);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 100);
                    contentStream.showText("Terms & Conditions:");
                    contentStream.newLineAtOffset(0, -12);
                    contentStream.showText("• Please arrive 30 minutes before departure");
                    contentStream.newLineAtOffset(0, -12);
                    contentStream.showText("• Carry valid government ID proof");
                    contentStream.newLineAtOffset(0, -12);
                    contentStream.showText("• Ticket is non-transferable");
                    contentStream.newLineAtOffset(0, -12);
                    contentStream.showText("• For support: support@busreservation.com");
                    contentStream.endText();

                }

                document.save(filePath.toFile());
            }

            log.info("Ticket PDF generated successfully: {}", filePath);
            return filePath.toString();

        } catch (IOException | WriterException e) {
            log.error("Failed to generate ticket PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to generate ticket", e);
        }
    }

    /**
     * Generate QR code data string
     */
    private String generateQRData(Booking booking) {
        return String.format(
            "BUS_TICKET|ID:%s|PASSENGER:%s|ROUTE:%s-%s|DATE:%s|TIME:%s|BUS:%s|SEATS:%s|AMOUNT:%.2f",
            booking.getId(),
            booking.getUser().getName().replace("|", ""),
            booking.getTrip().getRoute().getSource().replace("|", ""),
            booking.getTrip().getRoute().getDestination().replace("|", ""),
            booking.getTrip().getDepartureTime().format(DATE_FORMATTER),
            booking.getTrip().getDepartureTime().format(TIME_FORMATTER),
            booking.getTrip().getBus().getBusNumber(),
            booking.getSeats().stream()
                    .map(Seat::getSeatNumber)
                    .reduce((s1, s2) -> s1 + "," + s2)
                    .orElse(""),
            booking.getTotalAmount()
        );
    }

    /**
     * Generate QR code image
     */
    private BufferedImage generateQRCodeImage(String text, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Get ticket PDF as byte array
     */
    public byte[] getTicketPdfBytes(Long bookingId) throws IOException {
        String fileName = "ticket-" + bookingId + ".pdf";
        Path filePath = Paths.get(TICKETS_DIR, fileName);
        
        if (Files.exists(filePath)) {
            return Files.readAllBytes(filePath);
        }
        
        // Generate PDF if it doesn't exist
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        
        generateTicketPdf(booking);
        return Files.readAllBytes(filePath);
    }

    /**
     * Generate QR code as base64 string
     */
    public String generateQRCodeBase64(Booking booking) {
        try {
            String qrData = generateQRData(booking);
            BufferedImage qrImage = generateQRCodeImage(qrData, 200, 200);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("Failed to generate QR code: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Delete ticket PDF
     */
    public boolean deleteTicketPdf(Long bookingId) {
        String fileName = "ticket-" + bookingId + ".pdf";
        Path filePath = Paths.get(TICKETS_DIR, fileName);
        
        try {
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Failed to delete ticket PDF: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get ticket details for API response
     */
    public Map<String, Object> getTicketDetails(Booking booking) {
        Map<String, Object> details = new HashMap<>();
        
        details.put("bookingId", booking.getId());
        details.put("passengerName", booking.getUser().getName());
        details.put("passengerEmail", booking.getUser().getEmail());
        details.put("passengerPhone", booking.getUser().getPhone());
        details.put("route", Map.of(
            "source", booking.getTrip().getRoute().getSource(),
            "destination", booking.getTrip().getRoute().getDestination(),
            "distance", booking.getTrip().getRoute().getDistance(),
            "duration", booking.getTrip().getRoute().getDuration()
        ));
        details.put("trip", Map.of(
            "departureTime", booking.getTrip().getDepartureTime(),
            "arrivalTime", booking.getTrip().getArrivalTime(),
            "fare", booking.getTrip().getFare()
        ));
        details.put("bus", Map.of(
            "number", booking.getTrip().getBus().getBusNumber(),
            "type", booking.getTrip().getBus().getBusType(),
            "operator", booking.getTrip().getBus().getOperatorName(),
            "totalSeats", booking.getTrip().getBus().getTotalSeats()
        ));
        details.put("seats", booking.getSeats().stream()
                .map(seat -> Map.of(
                    "number", seat.getSeatNumber(),
                    "type", seat.getSeatType(),
                    "booked", seat.isBooked()
                ))
                .toList());
        details.put("payment", Map.of(
            "amount", booking.getTotalAmount(),
            "status", booking.getPayment() != null ? booking.getPayment().getStatus() : "PENDING",
            "method", booking.getPayment() != null ? booking.getPayment().getPaymentMethod() : null
        ));
        details.put("bookingStatus", booking.getStatus());
        details.put("bookingDate", booking.getBookingDate());
        
        // Add QR code
        details.put("qrCode", generateQRCodeBase64(booking));
        
        return details;
    }

    /**
     * Verify QR code data
     */
    public boolean verifyQRCode(Long bookingId, String qrData) {
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found"));
            
            String expectedQrData = generateQRData(booking);
            return expectedQrData.equals(qrData);
            
        } catch (Exception e) {
            log.error("QR code verification failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parse QR code data
     */
    public Map<String, String> parseQRCodeData(String qrData) {
        Map<String, String> result = new HashMap<>();
        
        try {
            String[] parts = qrData.split("\\|");
            for (String part : parts) {
                if (part.contains(":")) {
                    String[] keyValue = part.split(":", 2);
                    if (keyValue.length == 2) {
                        result.put(keyValue[0], keyValue[1]);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse QR code data: {}", e.getMessage());
        }
        
        return result;
    }

    /**
     * Check if ticket exists
     */
    public boolean ticketExists(Long bookingId) {
        String fileName = "ticket-" + bookingId + ".pdf";
        Path filePath = Paths.get(TICKETS_DIR, fileName);
        return Files.exists(filePath);
    }

    /**
     * Get ticket file path
     */
    public String getTicketFilePath(Long bookingId) {
        String fileName = "ticket-" + bookingId + ".pdf";
        Path filePath = Paths.get(TICKETS_DIR, fileName);
        return filePath.toString();
    }
}