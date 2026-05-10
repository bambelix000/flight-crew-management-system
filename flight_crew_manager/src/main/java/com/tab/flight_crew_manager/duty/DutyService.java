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
import java.time.LocalDateTime;
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

        return dutyRepository.save(savedDuty);
    }



    @Transactional
    public String assignUserToDuty(Long userId, Long dutyId, RoleOnDuty role) {
        User user = userRepository.findById(userId).orElseThrow();
        Duty duty = dutyRepository.findById(dutyId).orElseThrow();
        LocalDateTime newDutyStart = duty.getDutyStartTime();

        for (CrewAssignment existing : user.getAssignments()) {
            if (existing.getStatus() == AssignmentStatus.REJECTED) continue;
            Duty d = existing.getDuty();
            if (!(duty.getDutyEndTime().isBefore(d.getDutyStartTime()) || duty.getDutyStartTime().isAfter(d.getDutyEndTime()))) {
                throw new IllegalStateException("BLOKADA: Nakładanie się służb!");
            }
            if (duty.getDutyStartTime().isAfter(d.getDutyEndTime())) {
                if (java.time.Duration.between(d.getDutyEndTime(), duty.getDutyStartTime()).toHours() < 12) {
                    throw new IllegalStateException("BLOKADA: Brak 12h odpoczynku po poprzedniej służbie.");
                }
            } else if (duty.getDutyEndTime().isBefore(d.getDutyStartTime())) {
                if (java.time.Duration.between(duty.getDutyEndTime(), d.getDutyStartTime()).toHours() < 12) {
                    throw new IllegalStateException("BLOKADA: Brak 12h odpoczynku przed kolejną służbą.");
                }
            }
        }

        int rolling20DaysMinutes = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .map(CrewAssignment::getDuty)
                .filter(d -> d.getDutyStartTime().isAfter(newDutyStart.minusDays(20)) && d.getDutyStartTime().isBefore(newDutyStart))
                .mapToInt(Duty::getAirTimeMinutes).sum() + duty.getAirTimeMinutes();

        int rolling365DaysMinutes = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .map(CrewAssignment::getDuty)
                .filter(d -> d.getDutyStartTime().isAfter(newDutyStart.minusDays(365)) && d.getDutyStartTime().isBefore(newDutyStart))
                .mapToInt(Duty::getAirTimeMinutes).sum() + duty.getAirTimeMinutes();

        if (rolling20DaysMinutes > 5400) throw new IllegalStateException("BLOKADA: Przekroczenie 90h w 20 dni od daty lotu.");
        if (rolling365DaysMinutes > 54000) throw new IllegalStateException("BLOKADA: Przekroczenie 900h w rok od daty lotu.");

        CrewAssignment assignment = new CrewAssignment(user, duty, role);
        assignment.setStatus(AssignmentStatus.PENDING);
        assignmentRepository.save(assignment);

        if (rolling20DaysMinutes >= 5100 || rolling365DaysMinutes >= 53700) {
            return "WARNING_LIMIT: Użytkownik przypisany, ale brakuje mniej niż 5h do limitu FTL!";
        }
        return "SUCCESS: Przypisano pomyślnie.";
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
        dto.setFlights(duty.getFlights().stream().map(f -> new FlightSummaryDto(f.getId(), f.getFlightNumber(), f.getDepartureAirport().getAirportCode() + " - " + f.getArrivalAirport().getAirportCode())).collect(Collectors.toList()));
        dto.setAssignedCrew(duty.getAssignments().stream().map(a -> new CrewMemberDto(a.getUser().getId(), a.getUser().getLogin(), a.getUser().getName(), a.getUser().getSurname(), a.getRoleOnDuty().name(), a.getStatus().name(), a.getRejectionReason())).collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public void acceptDuty(String login, Long dutyId) {
        User user = userRepository.findByLogin(login).orElseThrow();
        CrewAssignment a = user.getAssignments().stream()
                .filter(as -> as.getDuty().getId().equals(dutyId)).findFirst().orElseThrow();
        a.setStatus(AssignmentStatus.ACCEPTED);
        assignmentRepository.save(a);
    }


    @Transactional
    public void rejectDuty(String login, Long dutyId, String reason) {
        User user = userRepository.findByLogin(login).orElseThrow();
        Duty duty = dutyRepository.findById(dutyId).orElseThrow();
        CrewAssignment a = user.getAssignments().stream()
                .filter(as -> as.getDuty().getId().equals(dutyId)).findFirst().orElseThrow();
        a.setStatus(AssignmentStatus.REJECTED);
        a.setRejectionReason(reason);
        user.setIncapacityCounter(user.getIncapacityCounter() + 1);
        userRepository.save(user);
        assignmentRepository.save(a);
    }

    @Transactional
    public void removeUserFromDuty(Long userId, Long dutyId) {
        User user = userRepository.findById(userId).orElseThrow();
        Duty duty = dutyRepository.findById(dutyId).orElseThrow();
        CrewAssignment a = user.getAssignments().stream()
                .filter(as -> as.getDuty().getId().equals(dutyId)).findFirst().orElseThrow();
        user.getAssignments().remove(a);
        duty.getAssignments().remove(a);
        userRepository.save(user);
        dutyRepository.save(duty);
        assignmentRepository.delete(a);
    }
}
