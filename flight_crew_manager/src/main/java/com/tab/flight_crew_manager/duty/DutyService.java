package com.tab.flight_crew_manager.duty;

import com.tab.flight_crew_manager.crew_assignment.AssignmentStatus;
import com.tab.flight_crew_manager.crew_assignment.CrewAssignment;
import com.tab.flight_crew_manager.crew_assignment.CrewAssignmentRepository;
import com.tab.flight_crew_manager.duty.dto.CrewMemberDto;
import com.tab.flight_crew_manager.duty.dto.DutyDto;
import com.tab.flight_crew_manager.flight.Flight;
import com.tab.flight_crew_manager.flight.FlightRepository;
import com.tab.flight_crew_manager.flight.dto.FlightSummaryDto;
import com.tab.flight_crew_manager.user.User;
import com.tab.flight_crew_manager.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        for (CrewAssignment existingAssignment : user.getAssignments()) {

            if (existingAssignment.getStatus() == AssignmentStatus.REJECTED) {
                continue;
            }
            Duty existingDuty = existingAssignment.getDuty();

            if (!(duty.getDutyEndTime().isBefore(existingDuty.getDutyStartTime()) ||
                    duty.getDutyStartTime().isAfter(existingDuty.getDutyEndTime()))) {
                throw new IllegalStateException("BLOKADA: Wykryto nakładanie się służb w czasie!");
            }

            if (duty.getDutyStartTime().isAfter(existingDuty.getDutyEndTime())) {
                long hoursBetween = Duration.between(existingDuty.getDutyEndTime(), duty.getDutyStartTime()).toHours();
                if (hoursBetween < 20) {
                    throw new IllegalStateException("BLOKADA: Brak 20h odpoczynku. Odstęp wynosi tylko " + hoursBetween + "h po poprzedniej służbie!");
                }
            }
            else if (duty.getDutyEndTime().isBefore(existingDuty.getDutyStartTime())) {
                long hoursBetween = Duration.between(duty.getDutyEndTime(), existingDuty.getDutyStartTime()).toHours();
                if (hoursBetween < 20) {
                    throw new IllegalStateException("BLOKADA: Brak 20h odpoczynku przed kolejną zaplanowaną służbą!");
                }
            }
        }

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

    public List<DutyDto> getAllDuties() {
        return dutyRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<DutyDto> getMyDuties(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono usera: " + login));

        return user.getAssignments().stream()
                .map(assignment -> mapToDto(assignment.getDuty()))
                .collect(Collectors.toList());
    }

    private DutyDto mapToDto(Duty duty) {
        DutyDto dto = new DutyDto();

        dto.setId(duty.getId());
        dto.setDutyStartTime(duty.getDutyStartTime());
        dto.setDutyEndTime(duty.getDutyEndTime());
        dto.setWorkTimeMinutes(duty.getWorkTimeMinutes());
        dto.setAirTimeMinutes(duty.getAirTimeMinutes());

        dto.setFlights(duty.getFlights().stream()
                .map(f -> new FlightSummaryDto(
                        f.getId(),
                        f.getFlightNumber(),
                        f.getDepartureAirport().getAirportCode() + " - " + f.getArrivalAirport().getAirportCode()
                ))
                .collect(Collectors.toList()));

        dto.setAssignedCrew(duty.getAssignments().stream()
                .map(assignment -> new CrewMemberDto(
                        assignment.getUser().getId(),
                        assignment.getUser().getName(),
                        assignment.getUser().getSurname(),
                        assignment.getRoleOnDuty().name(),
                        assignment.getStatus().name()
                ))
                .collect(Collectors.toList()));

        return dto;
    }

    public void acceptDuty(String login, Long dutyId) {
        User user = userRepository.findByLogin(login).orElseThrow();

        CrewAssignment assignment = user.getAssignments().stream()
                .filter(a -> a.getDuty().getId().equals(dutyId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nie jesteś przypisany do tej służby."));

        assignment.setStatus(AssignmentStatus.ACCEPTED);
        assignmentRepository.save(assignment);
    }
    @Transactional
    public void reportIncapacity(String login, Long dutyId) {
        User user = userRepository.findByLogin(login).orElseThrow();
        Duty duty = dutyRepository.findById(dutyId).orElseThrow();

        CrewAssignment assignment = user.getAssignments().stream()
                .filter(a -> a.getDuty().getId().equals(dutyId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nie jesteś przypisany do tej służby."));

        user.setTwentyDaysAirTime(user.getTwentyDaysAirTime() - duty.getAirTimeMinutes());
        user.setAnnualAirTime(user.getAnnualAirTime() - duty.getAirTimeMinutes());

        user.setIncapacityCounter(user.getIncapacityCounter() + 1);

        userRepository.save(user);
        assignment.setStatus(AssignmentStatus.REJECTED);
        assignmentRepository.save(assignment);
    }

    @Transactional
    public void removeUserFromDuty(Long userId, Long dutyId) {
        User user = userRepository.findById(userId).orElseThrow();
        Duty duty = dutyRepository.findById(dutyId).orElseThrow();

        CrewAssignment assignment = user.getAssignments().stream()
                .filter(a -> a.getDuty().getId().equals(dutyId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Użytkownik nie jest przypisany do tej służby."));

        user.getAssignments().remove(assignment);
        duty.getAssignments().remove(assignment);

        userRepository.save(user);
        assignmentRepository.delete(assignment);
    }
}
