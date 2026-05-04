package com.tab.flight_crew_manager.airport;

import com.tab.flight_crew_manager.airport.dto.AirportCreateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AirportService {

    private final AirportRepository airportRepository;

    @Transactional
    public Airport addAirport(AirportCreateDto dto) {
        if (airportRepository.findByAirportCode(dto.getAirportCode()).isPresent()) {
            throw new IllegalArgumentException("Lotnisko o kodzie " + dto.getAirportCode() + " już istnieje w systemie!");
        }

        Airport airport = new Airport();
        airport.setAirportCode(dto.getAirportCode().toUpperCase());
        airport.setName(dto.getName());
        airport.setCity(dto.getCity());
        airport.setCountry(dto.getCountry());

        return airportRepository.save(airport);
    }
}