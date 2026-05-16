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
                Airport syd = airportRepository.findByAirportCode("SYD").orElseThrow();
                Airport lax = airportRepository.findByAirportCode("LAX").orElseThrow();
                Airport lhr = airportRepository.findByAirportCode("LHR").orElseThrow();
                Airport krk = airportRepository.findByAirportCode("KRK").orElseThrow();
                Airport cdg = airportRepository.findByAirportCode("CDG").orElseThrow();
                Airport gdn = airportRepository.findByAirportCode("GDN").orElseThrow();
                Airport fra = airportRepository.findByAirportCode("FRA").orElseThrow();

                Flight flight1 = new Flight();
                flight1.setFlightNumber("LO901");
                flight1.setDepartureAirport(waw);
                flight1.setArrivalAirport(syd);
                flight1.setDepartureTime(LocalDateTime.of(2026, 5, 13, 8, 0));
                flight1.setArrivalTime(LocalDateTime.of(2026, 5, 14, 8, 0));
                flight1.setDurationMinutes((int) ChronoUnit.MINUTES.between(flight1.getDepartureTime(), flight1.getArrivalTime()));

                Flight flight2 = new Flight();
                flight2.setFlightNumber("LO902");
                flight2.setDepartureAirport(syd);
                flight2.setArrivalAirport(lax);
                flight2.setDepartureTime(LocalDateTime.of(2026, 5, 14, 10, 0));
                flight2.setArrivalTime(LocalDateTime.of(2026, 5, 15, 14, 0));
                flight2.setDurationMinutes((int) ChronoUnit.MINUTES.between(flight2.getDepartureTime(), flight2.getArrivalTime()));

                Flight flight3 = new Flight();
                flight3.setFlightNumber("LO903");
                flight3.setDepartureAirport(lax);
                flight3.setArrivalAirport(waw);
                flight3.setDepartureTime(LocalDateTime.of(2026, 5, 15, 16, 0));
                flight3.setArrivalTime(LocalDateTime.of(2026, 5, 16, 22, 0));
                flight3.setDurationMinutes((int) ChronoUnit.MINUTES.between(flight3.getDepartureTime(), flight3.getArrivalTime()));

                Flight flight4 = new Flight();
                flight4.setFlightNumber("LO279");
                flight4.setDepartureAirport(waw);
                flight4.setArrivalAirport(lhr);
                flight4.setDepartureTime(LocalDateTime.of(2026, 5, 18, 8, 0));
                flight4.setArrivalTime(LocalDateTime.of(2026, 5, 18, 10, 30));
                flight4.setDurationMinutes((int) ChronoUnit.MINUTES.between(flight4.getDepartureTime(), flight4.getArrivalTime()));

                Flight flight5 = new Flight();
                flight5.setFlightNumber("LO280");
                flight5.setDepartureAirport(lhr);
                flight5.setArrivalAirport(waw);
                flight5.setDepartureTime(LocalDateTime.of(2026, 5, 18, 12, 0));
                flight5.setArrivalTime(LocalDateTime.of(2026, 5, 18, 14, 30));
                flight5.setDurationMinutes((int) ChronoUnit.MINUTES.between(flight5.getDepartureTime(), flight5.getArrivalTime()));

                Flight flight6 = new Flight();
                flight6.setFlightNumber("LO331");
                flight6.setDepartureAirport(krk);
                flight6.setArrivalAirport(cdg);
                flight6.setDepartureTime(LocalDateTime.of(2026, 5, 20, 9, 0));
                flight6.setArrivalTime(LocalDateTime.of(2026, 5, 20, 11, 15));
                flight6.setDurationMinutes((int) ChronoUnit.MINUTES.between(flight6.getDepartureTime(), flight6.getArrivalTime()));

                Flight flight7 = new Flight();
                flight7.setFlightNumber("LO332");
                flight7.setDepartureAirport(cdg);
                flight7.setArrivalAirport(krk);
                flight7.setDepartureTime(LocalDateTime.of(2026, 5, 20, 13, 0));
                flight7.setArrivalTime(LocalDateTime.of(2026, 5, 20, 15, 15));
                flight7.setDurationMinutes((int) ChronoUnit.MINUTES.between(flight7.getDepartureTime(), flight7.getArrivalTime()));

                Flight flight8 = new Flight();
                flight8.setFlightNumber("LO401");
                flight8.setDepartureAirport(gdn);
                flight8.setArrivalAirport(fra);
                flight8.setDepartureTime(LocalDateTime.of(2026, 5, 22, 7, 30));
                flight8.setArrivalTime(LocalDateTime.of(2026, 5, 22, 9, 0));
                flight8.setDurationMinutes((int) ChronoUnit.MINUTES.between(flight8.getDepartureTime(), flight8.getArrivalTime()));

                flightRepository.saveAll(List.of(flight1, flight2, flight3, flight4, flight5, flight6, flight7, flight8));
            }
        };
    }
}