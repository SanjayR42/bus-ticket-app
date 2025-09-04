package com.bus.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class BusTicketReservationManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(BusTicketReservationManagementApplication.class, args);
	}

}
