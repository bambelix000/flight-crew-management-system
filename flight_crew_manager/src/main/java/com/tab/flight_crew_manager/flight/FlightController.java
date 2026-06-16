package com.tab.flight_crew_manager.flight;

import com.tab.flight_crew_manager.flight.dto.FlightCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flights")
@RequiredArgsConstructor
public class FlightController {
    private final FlightService flightService;

    @GetMapping("/get")
    public List<Flight> getAllFlights() {
        return flightService.getFlights();
    }

    @PreAuthorize("hasAuthority('SCHEDULER') or hasAuthority('ADMIN')")
    @PostMapping("/add")
    public ResponseEntity<Flight> addFlight(@RequestBody FlightCreateDto request) {
        Flight newFlight = flightService.addFlight(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newFlight);
    }


}
