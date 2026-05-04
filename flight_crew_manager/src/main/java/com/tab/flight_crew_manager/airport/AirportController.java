package com.tab.flight_crew_manager.airport;

import com.tab.flight_crew_manager.airport.dto.AirportCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/airports")
@RequiredArgsConstructor
public class AirportController {

    private final AirportService airportService;

    @PostMapping
    public ResponseEntity<?> addAirport(@RequestBody AirportCreateDto request) {
        try {
            Airport newAirport = airportService.addAirport(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(newAirport);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}