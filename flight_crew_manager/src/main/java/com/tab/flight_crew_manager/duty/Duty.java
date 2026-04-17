package com.tab.flight_crew_manager.duty;

import com.tab.flight_crew_manager.crew_assignment.CrewAssignment;
import com.tab.flight_crew_manager.flight.Flight;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "duties")
@Getter
@Setter
@NoArgsConstructor
public class Duty {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "duty_seq")
    @SequenceGenerator(name = "duty_seq", sequenceName = "duty_seq", allocationSize = 1)
    private Long id;

    private LocalDateTime dutyStartTime;
    private LocalDateTime dutyEndTime;
    private Integer workTimeMinutes;
    private Integer airTimeMinutes;

    @OneToMany(mappedBy = "duty", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Flight> flights = new ArrayList<>();

    public void addFlight(Flight flight) {
        flights.add(flight);
        flight.setDuty(this);
    }

    @OneToMany(mappedBy = "duty", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrewAssignment> assignments = new ArrayList<>();

}
