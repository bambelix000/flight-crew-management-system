package com.tab.flight_crew_manager.airport;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="airports")
@Getter
@Setter
@NoArgsConstructor
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "airport_seq")
    @SequenceGenerator(name = "airport_seq", sequenceName = "airport_seq", allocationSize = 1)
    private Long id;
    private String name;
    private String city;
    private String country;
    private String airportCode;

    public Airport(String airportCode, String name,  String city, String country) {
        this.airportCode = airportCode;
        this.name = name;
        this.city = city;
        this.country = country;
    }
}
