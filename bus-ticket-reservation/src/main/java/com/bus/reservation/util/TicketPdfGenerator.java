package com.bus.reservation.util;

import com.bus.reservation.model.Booking;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
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

@Service
@Slf4j
public class TicketPdfGenerator {

    private static final String TICKETS_DIR = "tickets/";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Keep the original simple method for backward compatibility
    public String generateTicketPdf(Booking booking) {
        System.out.println("Generating PDF for booking: " + booking.getId());
        return "/tickets/ticket-" + booking.getId() + ".pdf";
    }

    // Enhanced PDF generation with QR code
    public String generateEnhancedTicketPdf(Booking booking) {
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
                    // Add header
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 750);
                    contentStream.showText("BUS TICKET");
                    contentStream.endText();

                    // Add booking details
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, 700);
                    contentStream.showText("Booking ID: " + booking.getId());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Passenger: " + booking.getUser().getName());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Route: " + booking.getTrip().getRoute().getSource() + 
                                          " to " + booking.getTrip().getRoute().getDestination());
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Departure: " + 
                                          booking.getTrip().getDepartureTime().format(DATE_FORMATTER) + " at " +
                                          booking.getTrip().getDepartureTime().format(TIME_FORMATTER));
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Bus: " + booking.getTrip().getBus().getBusNumber() + 
                                          " (" + booking.getTrip().getBus().getBusType() + ")");
                    contentStream.newLineAtOffset(0, -20);
                    
                    // Add seats
                    String seats = booking.getSeats().stream()
                            .map(seat -> seat.getSeatNumber())
                            .reduce((s1, s2) -> s1 + ", " + s2)
                            .orElse("");
                    contentStream.showText("Seats: " + seats);
                    contentStream.newLineAtOffset(0, -20);
                    contentStream.showText("Amount: ₹" + booking.getTotalAmount());
                    contentStream.endText();

                    // Generate and add QR code
                    String qrData = generateQRData(booking);
                    BufferedImage qrImage = generateQRCodeImage(qrData, 200, 200);
                    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(qrImage, "PNG", baos);
                    byte[] qrImageBytes = baos.toByteArray();
                    
                    PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, qrImageBytes, "qr-code");
                    contentStream.drawImage(pdImage, 400, 600, 100, 100);
                }

                document.save(filePath.toFile());
            }

            log.info("Enhanced ticket PDF generated: {}", filePath);
            return filePath.toString();

        } catch (IOException | WriterException e) {
            log.error("Failed to generate enhanced ticket PDF: {}", e.getMessage());
            // Fallback to simple method
            return generateTicketPdf(booking);
        }
    }

    public String generateQRData(Booking booking) {
        return String.format(
            "BookingID:%s|Passenger:%s|Route:%s-%s|Date:%s|Bus:%s|Seats:%s",
            booking.getId(),
            booking.getUser().getName(),
            booking.getTrip().getRoute().getSource(),
            booking.getTrip().getRoute().getDestination(),
            booking.getTrip().getDepartureTime().format(DATE_FORMATTER),
            booking.getTrip().getBus().getBusNumber(),
            booking.getSeats().stream()
                    .map(seat -> seat.getSeatNumber())
                    .reduce((s1, s2) -> s1 + "," + s2)
                    .orElse("")
        );
    }

    private BufferedImage generateQRCodeImage(String text, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    public byte[] getTicketPdfBytes(Long bookingId) throws IOException {
        String fileName = "ticket-" + bookingId + ".pdf";
        Path filePath = Paths.get(TICKETS_DIR, fileName);
        
        if (Files.exists(filePath)) {
            return Files.readAllBytes(filePath);
        }
        throw new IOException("Ticket file not found");
    }

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

    // Simple QR code generation for API responses
    public String generateQRCodeBase64(Booking booking) {
        try {
            String qrData = generateQRData(booking);
            BufferedImage qrImage = generateQRCodeImage(qrData, 150, 150);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            return java.util.Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("Failed to generate QR code: {}", e.getMessage());
            return null;
        }
    }
}