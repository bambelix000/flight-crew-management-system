package com.tab.flight_crew_manager.flight;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tab.flight_crew_manager.airport.Airport;
import com.tab.flight_crew_manager.duty.Duty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "flights")
@Getter
@Setter
@NoArgsConstructor
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flight_seq")
    @SequenceGenerator(name = "flight_seq", sequenceName = "flight_seq", allocationSize = 1)
    private Long id;

    private String flightNumber;

    @ManyToOne
    @JoinColumn(name = "departure_airport_id")
    private Airport departureAirport;
    @ManyToOne
    @JoinColumn(name = "arrival_airport_id")
    private Airport arrivalAirport;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    private Integer durationMinutes;

    @ManyToOne
    @JoinColumn(name = "duty_id")
    @JsonIgnore
    private Duty duty;
}
