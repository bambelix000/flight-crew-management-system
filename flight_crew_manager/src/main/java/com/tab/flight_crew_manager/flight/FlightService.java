package com.tab.flight_crew_manager.flight;

import com.tab.flight_crew_manager.airport.Airport;
import com.tab.flight_crew_manager.airport.AirportRepository;
import com.tab.flight_crew_manager.flight.dto.FlightCreateDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightService {

    @Autowired
    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;

    public Flight addFlight(FlightCreateDto dto) {
        Airport depAirport = airportRepository.findById(dto.getDepartureAirportId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono lotniska wylotu o ID: " + dto.getDepartureAirportId()));

        Airport arrAirport = airportRepository.findById(dto.getArrivalAirportId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono lotniska przylotu o ID: " + dto.getArrivalAirportId()));

        Flight flight = new Flight();
        flight.setFlightNumber(dto.getFlightNumber());
        flight.setDepartureAirport(depAirport);
        flight.setArrivalAirport(arrAirport);
        flight.setDepartureTime(dto.getDepartureTime());
        flight.setArrivalTime(dto.getArrivalTime());

        long duration = Duration.between(dto.getDepartureTime(), dto.getArrivalTime()).toMinutes();
        flight.setDurationMinutes((int) duration);

        return flightRepository.save(flight);
    }

    public List<Flight> getFlights() {
        return flightRepository.findAll();
    }

    @Transactional
    public List<Flight> addMultipleFlights(List<FlightCreateDto> dtos) {
        List<Flight> flightsToSave = new ArrayList<>();

        for (FlightCreateDto dto : dtos) {
            Airport depAirport = airportRepository.findById(dto.getDepartureAirportId())
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono lotniska wylotu o ID: " + dto.getDepartureAirportId()));

            Airport arrAirport = airportRepository.findById(dto.getArrivalAirportId())
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono lotniska przylotu o ID: " + dto.getArrivalAirportId()));

            Flight flight = new Flight();
            flight.setFlightNumber(dto.getFlightNumber());
            flight.setDepartureAirport(depAirport);
            flight.setArrivalAirport(arrAirport);
            flight.setDepartureTime(dto.getDepartureTime());
            flight.setArrivalTime(dto.getArrivalTime());

            long duration = Duration.between(dto.getDepartureTime(), dto.getArrivalTime()).toMinutes();
            flight.setDurationMinutes((int) duration);

            flightsToSave.add(flight);
        }

        return flightRepository.saveAll(flightsToSave);
    }
}
