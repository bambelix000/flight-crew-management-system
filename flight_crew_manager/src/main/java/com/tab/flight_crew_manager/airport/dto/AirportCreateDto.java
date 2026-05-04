package com.tab.flight_crew_manager.airport.dto;

import lombok.Data;

@Data
public class AirportCreateDto {
    private String airportCode;
    private String name;
    private String city;
    private String country;
}