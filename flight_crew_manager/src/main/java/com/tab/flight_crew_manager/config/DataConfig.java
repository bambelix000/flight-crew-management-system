package com.tab.flight_crew_manager.config;

import com.tab.flight_crew_manager.airport.Airport;
import com.tab.flight_crew_manager.airport.AirportRepository;
import com.tab.flight_crew_manager.flight.Flight;
import com.tab.flight_crew_manager.flight.FlightRepository;
import com.tab.flight_crew_manager.user.User;
import com.tab.flight_crew_manager.user.UserRepository;
import com.tab.flight_crew_manager.user.UserRole;
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

                userRepository.save(jasiu);

                User stefan = new User(
                        UserRole.ADMIN,
                        "stefek",
                        passwordEncoder.encode("pass"),
                        "Stefan",
                        "Burczymucha",
                        "+48123321123"
                );

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
                List<Airport> airports = List.of(
                        new Airport("WAW", "Chopin Airport", "Warsaw", "Poland"),
                        new Airport("KRK", "John Paul II Balice", "Krakow", "Poland"),
                        new Airport("GDN", "Lech Walesa", "Gdansk", "Poland"),
                        new Airport("KTW", "Pyrzowice", "Katowice", "Poland"),
                        new Airport("WRO", "Copernicus", "Wroclaw", "Poland"),
                        new Airport("WMI", "Modlin", "Warsaw", "Poland"),
                        new Airport("POZ", "Lawica", "Poznan", "Poland"),
                        new Airport("RZE", "Jasionka", "Rzeszow", "Poland"),
                        new Airport("SZZ", "Solidarity", "Szczecin", "Poland"),
                        new Airport("LUZ", "Lublin Airport", "Lublin", "Poland"),
                        new Airport("BZG", "Bydgoszcz Ignacy Jan Paderewski", "Bydgoszcz", "Poland"),
                        new Airport("IEG", "Zielona Gora-Babimost", "Zielona Gora", "Poland"),
                        new Airport("LCJ", "Wladyslaw Reymont", "Lodz", "Poland"),
                        new Airport("SZY", "Olsztyn-Mazury", "Szymany", "Poland"),

                        new Airport("LHR", "Heathrow", "London", "UK"),
                        new Airport("LGW", "Gatwick", "London", "UK"),
                        new Airport("CDG", "Charles de Gaulle", "Paris", "France"),
                        new Airport("ORY", "Orly", "Paris", "France"),
                        new Airport("FRA", "Frankfurt am Main", "Frankfurt", "Germany"),
                        new Airport("MUC", "Munich Airport", "Munich", "Germany"),
                        new Airport("AMS", "Schiphol", "Amsterdam", "Netherlands"),
                        new Airport("MAD", "Adolfo Suarez Barajas", "Madrid", "Spain"),
                        new Airport("BCN", "El Prat", "Barcelona", "Spain"),
                        new Airport("FCO", "Fiumicino", "Rome", "Italy"),
                        new Airport("MXP", "Malpensa", "Milan", "Italy"),
                        new Airport("VIE", "Vienna International", "Vienna", "Austria"),
                        new Airport("ZRH", "Zurich Airport", "Zurich", "Switzerland"),
                        new Airport("CPH", "Kastrup", "Copenhagen", "Denmark"),
                        new Airport("OSL", "Gardermoen", "Oslo", "Norway"),
                        new Airport("ARN", "Arlanda", "Stockholm", "Sweden"),
                        new Airport("HEL", "Gardermoen", "Helsinki", "Finland"),
                        new Airport("DUB", "Dublin Airport", "Dublin", "Ireland"),
                        new Airport("LIS", "Humberto Delgado", "Lisbon", "Portugal"),
                        new Airport("ATH", "Eleftherios Venizelos", "Athens", "Greece"),
                        new Airport("BRU", "Brussels Airport", "Brussels", "Belgium"),
                        new Airport("PRG", "Vaclav Havel", "Prague", "Czechia"),
                        new Airport("BUD", "Ferenc Liszt", "Budapest", "Hungary"),
                        new Airport("OTP", "Ferenc Liszt", "Bucharest", "Romania"),

                        new Airport("JFK", "John F. Kennedy", "New York", "USA"),
                        new Airport("EWR", "O'Hare", "Chicago", "USA"),
                        new Airport("LAX", "Los Angeles International", "Los Angeles", "USA"),
                        new Airport("SFO", "San Francisco International", "San Francisco", "USA"),
                        new Airport("MIA", "Miami International", "Miami", "USA"),
                        new Airport("YYZ", "Pearson", "Toronto", "Canada"),
                        new Airport("DXB", "Dubai International", "Dubai", "UAE"),
                        new Airport("DOH", "Hamad International", "Doha", "Qatar"),
                        new Airport("HND", "Haneda", "Tokyo", "Japan"),
                        new Airport("NRT", "Narita", "Tokyo", "Japan"),
                        new Airport("SIN", "Changi", "Singapore", "Singapore"),
                        new Airport("HKG", "Hong Kong International", "Hong Kong", "Hong Kong"),
                        new Airport("BKK", "Suvarnabhumi", "Bangkok", "Thailand"),
                        new Airport("SYD", "Kingsford Smith", "Sydney", "Australia"),
                        new Airport("GRU", "Guarulhos", "Sao Paulo", "Brazil")
                );

                airportRepository.saveAll(airports);
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
