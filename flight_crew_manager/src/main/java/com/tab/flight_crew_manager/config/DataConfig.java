package com.tab.flight_crew_manager.config;

import com.tab.flight_crew_manager.flight.Flight;
import com.tab.flight_crew_manager.flight.FlightRepository;
import com.tab.flight_crew_manager.user.User;
import com.tab.flight_crew_manager.user.UserRepository;
import com.tab.flight_crew_manager.user.UserRole; // Upewnij się, że masz ten import
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Configuration
public class DataConfig {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, FlightRepository flightRepository) {
        return args -> {

            if (userRepository.count() == 0) {
                User jasiu = new User(
                        UserRole.CREWMEMBER,
                        "jan.kowalski",
                        "haslo123",
                        "Jan",
                        "Kowalski",
                        "+48123456789"
                );
                // Dajemy mu na start trochę wylatanych godzin (np. 80h = 4800 minut)
                jasiu.setTwentyDaysAirTime(4800);
                jasiu.setAnnualAirTime(20000);
                userRepository.save(jasiu);
            }

            if (flightRepository.count() == 0) {
                Flight flight1 = new Flight();
                flight1.setFlightNumber("LO279");
                flight1.setDepartureAirport("WAW");
                flight1.setArrivalAirport("LHR");
                flight1.setDepartureTime(LocalDateTime.of(2026, 4, 20, 8, 0));
                flight1.setArrivalTime(LocalDateTime.of(2026, 4, 20, 10, 30));
                flight1.setDurationMinutes((int) ChronoUnit.MINUTES.between(flight1.getDepartureTime(), flight1.getArrivalTime()));

                Flight flight2 = new Flight();
                flight2.setFlightNumber("LO280");
                flight2.setDepartureAirport("LHR");
                flight2.setArrivalAirport("WAW");
                flight2.setDepartureTime(LocalDateTime.of(2026, 4, 20, 12, 0)); // 1.5h przerwy w Londynie
                flight2.setArrivalTime(LocalDateTime.of(2026, 4, 20, 14, 30));
                flight2.setDurationMinutes((int) ChronoUnit.MINUTES.between(flight2.getDepartureTime(), flight2.getArrivalTime()));

                flightRepository.saveAll(List.of(flight1, flight2));
            }
        };
    }
}
