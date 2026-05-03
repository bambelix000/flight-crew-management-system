package com.tab.flight_crew_manager.flight.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FlightCreateDto {
    private String flightNumber;
    private Long departureAirportId;
    private Long arrivalAirportId;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
}