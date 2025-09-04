package com.bus.reservation.controller;

import com.bus.reservation.service.ReportService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    
    @GetMapping("/revenue/total")
    public ResponseEntity<Double> getTotalRevenue() {
        return ResponseEntity.ok(reportService.getTotalRevenue());
    }

  
    @GetMapping("/bookings/daily")
    public ResponseEntity<Map<?, Long>> getDailyBookings() {
        return ResponseEntity.ok(reportService.getDailyBookingSummary());
    }

   
    @GetMapping("/revenue/by-route")
    public ResponseEntity<Map<String, Double>> getRevenueByRoute() {
        return ResponseEntity.ok(reportService.getRevenueByRoute());
    }

    
    @GetMapping("/revenue/top-routes")
    public ResponseEntity<List<Map.Entry<String, Double>>> getTopRoutes(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(reportService.getTopRoutes(limit));
    }
}
