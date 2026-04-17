package com.tab.flight_crew_manager.duty;

import com.tab.flight_crew_manager.crew_assignment.CrewAssignment;
import com.tab.flight_crew_manager.crew_assignment.CrewAssignmentRepository;
import com.tab.flight_crew_manager.flight.Flight;
import com.tab.flight_crew_manager.flight.FlightRepository;
import com.tab.flight_crew_manager.user.User;
import com.tab.flight_crew_manager.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@Service
public class DutyService {
    private final DutyRepository dutyRepository;
    private final UserRepository userRepository;
    private final CrewAssignmentRepository assignmentRepository;
    private final FlightRepository flightRepository;

    @Autowired
    public DutyService(DutyRepository dutyRepository, UserRepository userRepository, CrewAssignmentRepository assignmentRepository, FlightRepository flightRepository) {
        this.dutyRepository = dutyRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
        this.flightRepository = flightRepository;
    }

    @Transactional
    public Duty createDutyFromFlights(List<Long> flightIds) {
        if (flightIds == null || flightIds.isEmpty()) {
            throw new IllegalArgumentException("Nie można utworzyć służby bez lotów.");
        }

        List<Flight> selectedFlights = flightRepository.findAllById(flightIds);

        selectedFlights.sort(Comparator.comparing(Flight::getDepartureTime));

        Flight firstFlight = selectedFlights.get(0);
        Flight lastFlight = selectedFlights.get(selectedFlights.size() - 1);

        Duty newDuty = new Duty();

        newDuty.setDutyStartTime(firstFlight.getDepartureTime().minusHours(1));

        newDuty.setDutyEndTime(lastFlight.getArrivalTime());

        int totalAirTime = selectedFlights.stream()
                .mapToInt(Flight::getDurationMinutes)
                .sum();
        newDuty.setAirTimeMinutes(totalAirTime);

        long workTimeMinutes = Duration.between(firstFlight.getDepartureTime(), lastFlight.getArrivalTime()).toMinutes();
        newDuty.setWorkTimeMinutes((int) workTimeMinutes);

        Duty savedDuty = dutyRepository.save(newDuty);

        for (Flight flight : selectedFlights) {
            savedDuty.addFlight(flight);
        }

        return dutyRepository.save(savedDuty); // Aktualizujemy z przypisanymi lotami
    }


    @Transactional
    public void assignUserToDuty(Long userId, Long dutyId, RoleOnDuty role) {
        User user = userRepository.findById(userId).orElseThrow();
        Duty duty = dutyRepository.findById(dutyId).orElseThrow();

        int planned20DaysAirTime = user.getTwentyDaysAirTime() + duty.getAirTimeMinutes();
        int plannedAnnualAirTime = user.getAnnualAirTime() + duty.getAirTimeMinutes();

        if (planned20DaysAirTime > 5400) {
            throw new IllegalStateException("BLOKADA: Zaplanowanie tego lotu przekroczy limit 90h w ciągu 20 dni!");
        }
        if (plannedAnnualAirTime > 54000) {
            throw new IllegalStateException("BLOKADA: Zaplanowanie tego lotu przekroczy roczny limit 900h!");
        }

        user.setTwentyDaysAirTime(planned20DaysAirTime);
        user.setAnnualAirTime(plannedAnnualAirTime);
        userRepository.save(user);

        CrewAssignment assignment = new CrewAssignment(user, duty, role);
        assignmentRepository.save(assignment);

    }



}
