package com.bus.reservation.dto;

import lombok.Data;

@Data
public class BookingRequest {
    private Long busId;
    private Long userId;
    private int seats;
}
