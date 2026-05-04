package com.tab.flight_crew_manager.config;

import com.tab.flight_crew_manager.airport.Airport;
import com.tab.flight_crew_manager.airport.AirportRepository;
import com.tab.flight_crew_manager.flight.Flight;
import com.tab.flight_crew_manager.flight.FlightRepository;
import com.tab.flight_crew_manager.user.User;
import com.tab.flight_crew_manager.user.UserRepository;
import com.tab.flight_crew_manager.user.UserRole; // Upewnij się, że masz ten import
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Configuration
public class DataConfig {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, FlightRepository flightRepository, PasswordEncoder passwordEncoder, AirportRepository airportRepository) {
        return args -> {

            if (userRepository.count() == 0) {
                User jasiu = new User(
                        UserRole.CREWMEMBER,
                        "jan.kowalski",
                        passwordEncoder.encode("haslo123"),
                        "Jan",
                        "Kowalski",
                        "+48123456789"
                );
                jasiu.setTwentyDaysAirTime(5340);
                jasiu.setAnnualAirTime(54000);
                userRepository.save(jasiu);

                User stefan = new User(
                        UserRole.ADMIN,
                        "stefek",
                        passwordEncoder.encode("pass"),
                        "Stefan",
                        "Burczymucha",
                        "+48123321123"
                );
                stefan.setTwentyDaysAirTime(2400);
                stefan.setAnnualAirTime(12000);
                userRepository.save(stefan);

                User mirek = new User(
                        UserRole.SCHEDULER,
                        "mirek",
                        passwordEncoder.encode("pass"),
                        "Mirosław",
                        "Stabiński",
                        "+48213213213"
                );
                userRepository.save(mirek);
            }
            if (airportRepository.count() == 0) {

                Airport waw = new Airport("WAW", "Chopin Airport", "Warsaw", "Poland");
                Airport lhr = new Airport("LHR", "Heathrow Airport", "London", "UK");
                Airport jfk = new Airport("JFK", "John F. Kennedy", "New York", "USA");
                Airport cdg = new Airport("CDG", "Charles de Gaulle", "Paris", "France");
                Airport fco = new Airport("FCO", "Fiumicino", "Rome", "Italy");

                airportRepository.saveAll(List.of(waw, lhr, jfk, cdg, fco));
            }
            if (flightRepository.count() == 0) {
                Airport waw = airportRepository.findByAirportCode("WAW").orElseThrow();
                Airport lhr = airportRepository.findByAirportCode("LHR").orElseThrow();

                Flight flight1 = new Flight();
                flight1.setFlightNumber("LO279");
                flight1.setDepartureAirport(waw);
                flight1.setArrivalAirport(lhr);
                flight1.setDepartureTime(LocalDateTime.of(2026, 4, 20, 8, 0));
                flight1.setArrivalTime(LocalDateTime.of(2026, 4, 20, 10, 30));
                flight1.setDurationMinutes((int) ChronoUnit.MINUTES.between(flight1.getDepartureTime(), flight1.getArrivalTime()));

                Flight flight2 = new Flight();
                flight2.setFlightNumber("LO280");
                flight2.setDepartureAirport(lhr);
                flight2.setArrivalAirport(waw);
                flight2.setDepartureTime(LocalDateTime.of(2026, 4, 20, 12, 0)); // 1.5h przerwy w Londynie
                flight2.setArrivalTime(LocalDateTime.of(2026, 4, 20, 14, 30));
                flight2.setDurationMinutes((int) ChronoUnit.MINUTES.between(flight2.getDepartureTime(), flight2.getArrivalTime()));

                flightRepository.saveAll(List.of(flight1, flight2));
            }
        };
    }
}
