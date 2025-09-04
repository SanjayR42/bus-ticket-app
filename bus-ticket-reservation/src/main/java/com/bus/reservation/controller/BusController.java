package com.bus.reservation.controller;

import com.bus.reservation.model.Bus;
import com.bus.reservation.model.Trip;
import com.bus.reservation.service.BusService;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1/buses")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class BusController {

    private final BusService busService;

    // Admin only
    @PostMapping
    public ResponseEntity<Bus> addBus(@RequestBody Bus bus) {
        return ResponseEntity.ok(busService.addBus(bus));
    }

    // All authenticated users
    @GetMapping
    public ResponseEntity<List<Bus>> getAllBuses() {
        List<Bus> buses = busService.getAllBuses();
        if (buses.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList()); // Return empty array instead of error
        }
        return ResponseEntity.ok(buses);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Trip>> searchBuses(
            @RequestParam String source,
            @RequestParam String destination) {
        List<Trip> buses = busService.searchBuses(source, destination);
        return ResponseEntity.ok(buses);
    }
}
