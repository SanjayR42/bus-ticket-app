package com.bus.reservation.service;

import com.bus.reservation.model.Payment;
import com.bus.reservation.repository.BookingRepository;
import com.bus.reservation.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    // Total revenue across all payments
    public Double getTotalRevenue() {
        return paymentRepository.findAll()
                .stream()
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    // Daily booking summary
    public Map<LocalDate, Long> getDailyBookingSummary() {
        return bookingRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        b -> b.getTrip().getDepartureTime().toLocalDate(),
                        Collectors.counting()
                ));
    }

    // Revenue by route
    public Map<String, Double> getRevenueByRoute() {
        return paymentRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        p -> p.getBooking().getTrip().getRoute().getSource() + " → " +
                             p.getBooking().getTrip().getRoute().getDestination(),
                        Collectors.summingDouble(Payment::getAmount)
                ));
    }

    // Top N routes by revenue
    public List<Map.Entry<String, Double>> getTopRoutes(int limit) {
        return getRevenueByRoute().entrySet()
                .stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .toList();
    }
}
