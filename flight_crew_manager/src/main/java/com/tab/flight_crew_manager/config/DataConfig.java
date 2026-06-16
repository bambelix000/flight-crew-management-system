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

                userRepository.saveAll(List.of(
                        new User(UserRole.CREWMEMBER,"jan.kowalski",passwordEncoder.encode("pass"),"Jan","Kowalski","+48123456789"),
                        new User(UserRole.CREWMEMBER, "anna.nowak", passwordEncoder.encode("pass"), "Anna", "Nowak", "+48500100100"),
                        new User(UserRole.CREWMEMBER, "piotr.wisniewski", passwordEncoder.encode("pass"), "Piotr", "Wisniewski", "+48500100101"),
                        new User(UserRole.CREWMEMBER, "marta.wojcik", passwordEncoder.encode("pass"), "Marta", "Wojcik", "+48500100102"),
                        new User(UserRole.CREWMEMBER, "tomasz.kaminski", passwordEncoder.encode("pass"), "Tomasz", "Kaminski", "+48500100103"),
                        new User(UserRole.CREWMEMBER, "karolina.lewandowska", passwordEncoder.encode("pass"), "Karolina", "Lewandowska", "+48500100104"),
                        new User(UserRole.CREWMEMBER, "adam.zielinski", passwordEncoder.encode("pass"), "Adam", "Zielinski", "+48500100105"),
                        new User(UserRole.CREWMEMBER, "ewa.szymanska", passwordEncoder.encode("pass"), "Ewa", "Szymanska", "+48500100106"),
                        new User(UserRole.CREWMEMBER, "kamil.wozniak", passwordEncoder.encode("pass"), "Kamil", "Wozniak", "+48500100107")
                ));

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
                Airport krk = airportRepository.findByAirportCode("KRK").orElseThrow();
                Airport cdg = airportRepository.findByAirportCode("CDG").orElseThrow();
                Airport gdn = airportRepository.findByAirportCode("GDN").orElseThrow();
                Airport fra = airportRepository.findByAirportCode("FRA").orElseThrow();
                Airport ams = airportRepository.findByAirportCode("AMS").orElseThrow();
                Airport mad = airportRepository.findByAirportCode("MAD").orElseThrow();
                Airport bcn = airportRepository.findByAirportCode("BCN").orElseThrow();
                Airport cph = airportRepository.findByAirportCode("CPH").orElseThrow();
                Airport arn = airportRepository.findByAirportCode("ARN").orElseThrow();
                Airport vie = airportRepository.findByAirportCode("VIE").orElseThrow();
                Airport bud = airportRepository.findByAirportCode("BUD").orElseThrow();
                Airport dxb = airportRepository.findByAirportCode("DXB").orElseThrow();
                Airport sin = airportRepository.findByAirportCode("SIN").orElseThrow();
                Airport syd = airportRepository.findByAirportCode("SYD").orElseThrow();
                Airport hnd = airportRepository.findByAirportCode("HND").orElseThrow();
                Airport lax = airportRepository.findByAirportCode("LAX").orElseThrow();
                Airport jfk = airportRepository.findByAirportCode("JFK").orElseThrow();
                Airport doh = airportRepository.findByAirportCode("DOH").orElseThrow();
                Airport hkg = airportRepository.findByAirportCode("HKG").orElseThrow();
                Airport bkk = airportRepository.findByAirportCode("BKK").orElseThrow();

                List<Flight> seededFlights = List.of(
                        // Zmiana A: WAW -> LHR -> WAW, 5h w powietrzu.
                        createFlight("LO111", waw, lhr, LocalDateTime.of(2026, 6, 11, 8, 0), LocalDateTime.of(2026, 6, 11, 10, 30)),
                        createFlight("LO112", lhr, waw, LocalDateTime.of(2026, 6, 11, 12, 0), LocalDateTime.of(2026, 6, 11, 14, 30)),

                        // Zmiana B: KRK -> CDG -> KRK, 4h30 w powietrzu.
                        createFlight("LO221", krk, cdg, LocalDateTime.of(2026, 6, 12, 9, 0), LocalDateTime.of(2026, 6, 12, 11, 15)),
                        createFlight("LO222", cdg, krk, LocalDateTime.of(2026, 6, 12, 12, 45), LocalDateTime.of(2026, 6, 12, 15, 0)),

                        // Zmiana C: WAW -> FRA -> AMS -> WAW, 5h w powietrzu.
                        createFlight("LO331", waw, fra, LocalDateTime.of(2026, 6, 14, 7, 0), LocalDateTime.of(2026, 6, 14, 8, 45)),
                        createFlight("LO332", fra, ams, LocalDateTime.of(2026, 6, 14, 10, 0), LocalDateTime.of(2026, 6, 14, 11, 15)),
                        createFlight("LO333", ams, waw, LocalDateTime.of(2026, 6, 14, 12, 45), LocalDateTime.of(2026, 6, 14, 14, 45)),

                        // Zmiana D: GDN -> CPH -> ARN -> GDN, 4h w powietrzu.
                        createFlight("LO441", gdn, cph, LocalDateTime.of(2026, 6, 16, 6, 30), LocalDateTime.of(2026, 6, 16, 7, 50)),
                        createFlight("LO442", cph, arn, LocalDateTime.of(2026, 6, 16, 9, 15), LocalDateTime.of(2026, 6, 16, 10, 35)),
                        createFlight("LO443", arn, gdn, LocalDateTime.of(2026, 6, 16, 12, 0), LocalDateTime.of(2026, 6, 16, 13, 20)),

                        // Zmiana E: WAW -> MAD -> BCN -> WAW, 8h w powietrzu.
                        createFlight("LO551", waw, mad, LocalDateTime.of(2026, 6, 18, 7, 30), LocalDateTime.of(2026, 6, 18, 11, 0)),
                        createFlight("LO552", mad, bcn, LocalDateTime.of(2026, 6, 18, 12, 30), LocalDateTime.of(2026, 6, 18, 13, 50)),
                        createFlight("LO553", bcn, waw, LocalDateTime.of(2026, 6, 18, 15, 0), LocalDateTime.of(2026, 6, 18, 18, 10)),

                        // Zmiana F: KRK -> VIE -> BUD -> KRK, 3h w powietrzu.
                        createFlight("LO661", krk, vie, LocalDateTime.of(2026, 6, 20, 8, 0), LocalDateTime.of(2026, 6, 20, 9, 0)),
                        createFlight("LO662", vie, bud, LocalDateTime.of(2026, 6, 20, 10, 15), LocalDateTime.of(2026, 6, 20, 11, 10)),
                        createFlight("LO663", bud, krk, LocalDateTime.of(2026, 6, 20, 12, 40), LocalDateTime.of(2026, 6, 20, 13, 45)),

                        // Absurdalna zmiana demo: LO901-LO912 lacznie 86h w powietrzu.
                        createFlight("LO901", waw, dxb, LocalDateTime.of(2026, 6, 23, 6, 0), LocalDateTime.of(2026, 6, 23, 11, 30)),
                        createFlight("LO902", dxb, sin, LocalDateTime.of(2026, 6, 23, 13, 0), LocalDateTime.of(2026, 6, 23, 20, 30)),
                        createFlight("LO903", sin, syd, LocalDateTime.of(2026, 6, 23, 22, 0), LocalDateTime.of(2026, 6, 24, 6, 0)),
                        createFlight("LO904", syd, hnd, LocalDateTime.of(2026, 6, 24, 8, 0), LocalDateTime.of(2026, 6, 24, 17, 30)),
                        createFlight("LO905", hnd, lax, LocalDateTime.of(2026, 6, 24, 19, 0), LocalDateTime.of(2026, 6, 25, 5, 0)),
                        createFlight("LO906", lax, jfk, LocalDateTime.of(2026, 6, 25, 7, 0), LocalDateTime.of(2026, 6, 25, 12, 30)),
                        createFlight("LO907", jfk, lhr, LocalDateTime.of(2026, 6, 25, 14, 0), LocalDateTime.of(2026, 6, 25, 21, 0)),
                        createFlight("LO908", lhr, doh, LocalDateTime.of(2026, 6, 25, 22, 30), LocalDateTime.of(2026, 6, 26, 5, 0)),
                        createFlight("LO909", doh, hkg, LocalDateTime.of(2026, 6, 26, 6, 30), LocalDateTime.of(2026, 6, 26, 14, 30)),
                        createFlight("LO910", hkg, bkk, LocalDateTime.of(2026, 6, 26, 16, 0), LocalDateTime.of(2026, 6, 26, 19, 0)),
                        createFlight("LO911", bkk, doh, LocalDateTime.of(2026, 6, 26, 20, 30), LocalDateTime.of(2026, 6, 27, 3, 30)),
                        createFlight("LO912", doh, waw, LocalDateTime.of(2026, 6, 27, 5, 0), LocalDateTime.of(2026, 6, 27, 13, 30))
                );

                flightRepository.saveAll(seededFlights);
            }
        };
    }

    private Flight createFlight(String flightNumber, Airport departureAirport, Airport arrivalAirport,
                                LocalDateTime departureTime, LocalDateTime arrivalTime) {
        Flight flight = new Flight();
        flight.setFlightNumber(flightNumber);
        flight.setDepartureAirport(departureAirport);
        flight.setArrivalAirport(arrivalAirport);
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(arrivalTime);
        flight.setDurationMinutes((int) ChronoUnit.MINUTES.between(departureTime, arrivalTime));
        return flight;
    }
}
