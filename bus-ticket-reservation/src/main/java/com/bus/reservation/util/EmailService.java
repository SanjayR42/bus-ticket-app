package com.bus.reservation.util;

import com.bus.reservation.model.Booking;
import com.bus.reservation.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final TicketPdfGenerator ticketPdfGenerator;

    // Keep the original simple method for backward compatibility
    public void sendBookingConfirmation(String email, String bookingDetails) {
        System.out.println("Sending booking confirmation to: " + email);
        System.out.println("Details: " + bookingDetails);
    }

    // Enhanced method with HTML templates
    public void sendBookingConfirmation(Booking booking) {
        try {
            User user = booking.getUser();
            String ticketPdfPath = ticketPdfGenerator.generateTicketPdf(booking);
            
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("booking", booking);
            context.setVariable("trip", booking.getTrip());
            context.setVariable("seats", booking.getSeats());

            String htmlContent = templateEngine.process("email/booking-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(user.getEmail());
            helper.setSubject("Booking Confirmation - #" + booking.getId());
            helper.setText(htmlContent, true);
            
            // Note: File attachment would require FileSystem access
            // helper.addAttachment("ticket.pdf", new File(ticketPdfPath));

            mailSender.send(message);
            log.info("Booking confirmation email sent to: {}", user.getEmail());
            
        } catch (MessagingException e) {
            log.error("Failed to send booking confirmation email: {}", e.getMessage());
            // Fallback to simple method
            sendBookingConfirmation(booking.getUser().getEmail(), 
                "Booking #" + booking.getId() + " confirmed. Amount: " + booking.getTotalAmount());
        } catch (Exception e) {
            log.error("Unexpected error sending email: {}", e.getMessage());
        }
    }

    public void sendPaymentReceipt(Booking booking) {
        try {
            User user = booking.getUser();
            
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("booking", booking);
            context.setVariable("payment", booking.getPayment());

            String htmlContent = templateEngine.process("email/payment-receipt", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(user.getEmail());
            helper.setSubject("Payment Receipt - Booking #" + booking.getId());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Payment receipt email sent to: {}", user.getEmail());
            
        } catch (MessagingException e) {
            log.error("Failed to send payment receipt email: {}", e.getMessage());
        }
    }

    public void sendCancellationConfirmation(Booking booking, double refundAmount) {
        try {
            User user = booking.getUser();
            
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("booking", booking);
            context.setVariable("refundAmount", refundAmount);

            String htmlContent = templateEngine.process("email/booking-cancellation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(user.getEmail());
            helper.setSubject("Booking Cancelled - #" + booking.getId());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Cancellation confirmation email sent to: {}", user.getEmail());
            
        } catch (MessagingException e) {
            log.error("Failed to send cancellation email: {}", e.getMessage());
        }
    }

    public void sendReminder(Booking booking) {
        try {
            User user = booking.getUser();
            
            Context context = new Context();
            context.setVariable("user", user);
            context.setVariable("booking", booking);
            context.setVariable("trip", booking.getTrip());

            String htmlContent = templateEngine.process("email/booking-reminder", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(user.getEmail());
            helper.setSubject("Reminder: Your trip is coming up - Booking #" + booking.getId());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Reminder email sent to: {}", user.getEmail());
            
        } catch (MessagingException e) {
            log.error("Failed to send reminder email: {}", e.getMessage());
        }
    }

    // Simple text email fallback
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text);

            mailSender.send(message);
            log.info("Simple email sent to: {}", to);
            
        } catch (MessagingException e) {
            log.error("Failed to send simple email: {}", e.getMessage());
        }
    }
}