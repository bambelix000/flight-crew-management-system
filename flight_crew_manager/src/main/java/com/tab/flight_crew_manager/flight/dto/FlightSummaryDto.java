package com.tab.flight_crew_manager.flight.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FlightSummaryDto {
    private Long id;
    private String flightNumber;
    private String route; // np. "WAW - LHR"
}