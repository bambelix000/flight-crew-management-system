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

import java.util.ArrayList;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        List<Long> uniqueFlightIds = flightIds.stream().distinct().toList();
        List<Flight> selectedFlights = flightRepository.findAllByIdForUpdate(uniqueFlightIds);

        if (selectedFlights.size() != uniqueFlightIds.size()) {
            throw new IllegalArgumentException("Nie znaleziono wszystkich wybranych lotow.");
        }

        List<String> alreadyAssignedFlights = selectedFlights.stream()
                .filter(flight -> flight.getDuty() != null)
                .map(Flight::getFlightNumber)
                .toList();

        if (!alreadyAssignedFlights.isEmpty()) {
            throw new IllegalStateException("Te loty sa juz przypisane do sluzby: " + String.join(", ", alreadyAssignedFlights));
        }

        Duty newDuty = new Duty();
        recalculateDutyTimes(newDuty, selectedFlights);

        Duty savedDuty = dutyRepository.save(newDuty);

        for (Flight flight : selectedFlights) {
            savedDuty.addFlight(flight);
        }

        return dutyRepository.save(savedDuty);
    }

    @Transactional
    public void updateDutyFlights(Long dutyId, List<Long> flightIds) {
        if (flightIds == null || flightIds.isEmpty()) {
            throw new IllegalArgumentException("Sluzba musi zawierac co najmniej jeden lot.");
        }

        Duty duty = dutyRepository.findByIdForUpdate(dutyId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono sluzby."));

        List<Long> uniqueFlightIds = flightIds.stream().distinct().toList();
        List<Flight> selectedFlights = flightRepository.findAllByIdForUpdate(uniqueFlightIds);

        if (selectedFlights.size() != uniqueFlightIds.size()) {
            throw new IllegalArgumentException("Nie znaleziono wszystkich wybranych lotow.");
        }

        List<String> alreadyAssignedFlights = selectedFlights.stream()
                .filter(flight -> flight.getDuty() != null && !flight.getDuty().getId().equals(dutyId))
                .map(Flight::getFlightNumber)
                .toList();

        if (!alreadyAssignedFlights.isEmpty()) {
            throw new IllegalStateException("Te loty sa juz przypisane do innej sluzby: " + String.join(", ", alreadyAssignedFlights));
        }

        List<CrewAssignment> activeAssignments = duty.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .toList();
        Map<Long, AssignmentTimeSnapshot> previousStats = new HashMap<>();
        for (CrewAssignment assignment : activeAssignments) {
            previousStats.put(assignment.getId(), snapshotAssignmentTime(assignment));
        }

        for (Flight flight : new ArrayList<>(duty.getFlights())) {
            flight.setDuty(null);
        }
        duty.getFlights().clear();

        selectedFlights.sort(Comparator.comparing(Flight::getDepartureTime));
        for (Flight flight : selectedFlights) {
            duty.addFlight(flight);
        }
        recalculateDutyTimes(duty, selectedFlights);

        for (CrewAssignment assignment : activeAssignments) {
            AssignmentTimeSnapshot previous = previousStats.get(assignment.getId());
            AssignmentTimeSnapshot current = snapshotAssignmentTime(assignment);
            User assignedUser = assignment.getUser();

            assignedUser.setTotalAirBorneTimeMinutes(Math.max(0,
                    safeMinutes(assignedUser.getTotalAirBorneTimeMinutes()) - previous.airTimeMinutes() + current.airTimeMinutes()));
            assignedUser.setTotalWorkTimeMinutes(Math.max(0,
                    safeMinutes(assignedUser.getTotalWorkTimeMinutes()) - previous.workTimeMinutes() + current.workTimeMinutes()));
            assignedUser.setTotalDutyTimeMinutes(Math.max(0,
                    safeMinutes(assignedUser.getTotalDutyTimeMinutes()) - previous.dutyTimeMinutes() + current.dutyTimeMinutes()));

            userRepository.save(assignedUser);
        }

        dutyRepository.save(duty);
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
                if (java.time.Duration.between(d.getDutyEndTime(), duty.getDutyStartTime()).toHours() < 20) {
                    throw new IllegalStateException("BLOKADA: Brak 20h odpoczynku po poprzedniej służbie.");
                }
            } else if (duty.getDutyEndTime().isBefore(d.getDutyStartTime())) {
                if (java.time.Duration.between(duty.getDutyEndTime(), d.getDutyStartTime()).toHours() < 20) {
                    throw new IllegalStateException("BLOKADA: Brak 20h odpoczynku przed kolejną służbą.");
                }
            }
        }

        int rolling20DaysMinutes = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .filter(a -> a.getDuty().getDutyStartTime().isAfter(newDutyStart.minusDays(20)) && a.getDuty().getDutyStartTime().isBefore(newDutyStart))
                .mapToInt(this::getTrackedAirTimeMinutes).sum() + getPlannedAirTimeMinutes(duty);

        int rolling365DaysMinutes = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .filter(a -> a.getDuty().getDutyStartTime().isAfter(newDutyStart.minusDays(365)) && a.getDuty().getDutyStartTime().isBefore(newDutyStart))
                .mapToInt(this::getTrackedAirTimeMinutes).sum() + getPlannedAirTimeMinutes(duty);

        if (rolling20DaysMinutes > 5400) throw new IllegalStateException("BLOKADA: Przekroczenie 90h w 20 dni od daty lotu.");
        if (rolling365DaysMinutes > 54000) throw new IllegalStateException("BLOKADA: Przekroczenie 900h w rok od daty lotu.");

        int dutyDurationMinutes = getPlannedDutyDurationMinutes(duty);

        user.setTotalAirBorneTimeMinutes((user.getTotalAirBorneTimeMinutes() != null ? user.getTotalAirBorneTimeMinutes() : 0) + getPlannedAirTimeMinutes(duty));
        user.setTotalWorkTimeMinutes((user.getTotalWorkTimeMinutes() != null ? user.getTotalWorkTimeMinutes() : 0) + getPlannedWorkTimeMinutes(duty));
        user.setTotalDutyTimeMinutes((user.getTotalDutyTimeMinutes() != null ? user.getTotalDutyTimeMinutes() : 0) + dutyDurationMinutes);

        CrewAssignment assignment = new CrewAssignment(user, duty, role);
        assignment.setStatus(AssignmentStatus.PENDING);

        userRepository.save(user);
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
        dto.setAssignedCrew(duty.getAssignments().stream()
                .map(a -> new CrewMemberDto(
                        a.getUser().getId(),
                        a.getUser().getLogin(),
                        a.getUser().getName(),
                        a.getUser().getSurname(),
                        a.getUser().getPhoneNumber(),
                        a.getRoleOnDuty().name(),
                        a.getStatus().name(),
                        a.getRejectionReason(),
                        a.getActualStartTime(),
                        a.getActualEndTime()))
                .collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public void startDuty(String login, Long dutyId) {
        CrewAssignment assignment = findAssignment(login, dutyId);

        if (assignment.getStatus() != AssignmentStatus.ACCEPTED) {
            throw new IllegalStateException("Sluzbe mozna rozpoczac dopiero po zaakceptowaniu.");
        }
        if (assignment.getActualStartTime() != null) {
            throw new IllegalStateException("Sluzba zostala juz rozpoczeta.");
        }

        assignment.setActualStartTime(LocalDateTime.now());
        assignmentRepository.save(assignment);
    }

    @Transactional
    public void stopDuty(String login, Long dutyId) {
        CrewAssignment assignment = findAssignment(login, dutyId);

        if (assignment.getStatus() != AssignmentStatus.ACCEPTED) {
            throw new IllegalStateException("Sluzbe mozna zakonczyc tylko po zaakceptowaniu.");
        }
        if (assignment.getActualStartTime() == null) {
            throw new IllegalStateException("Najpierw rozpocznij sluzbe.");
        }
        if (assignment.getActualEndTime() != null) {
            throw new IllegalStateException("Sluzba zostala juz zakonczona.");
        }

        LocalDateTime actualEndTime = LocalDateTime.now();
        assignment.setActualEndTime(actualEndTime);

        User user = assignment.getUser();
        Duty duty = assignment.getDuty();

        user.setTotalAirBorneTimeMinutes(Math.max(0,
                safeMinutes(user.getTotalAirBorneTimeMinutes()) - getPlannedAirTimeMinutes(duty) + getTrackedAirTimeMinutes(assignment)));
        user.setTotalWorkTimeMinutes(Math.max(0,
                safeMinutes(user.getTotalWorkTimeMinutes()) - getPlannedWorkTimeMinutes(duty) + getTrackedWorkTimeMinutes(assignment)));
        user.setTotalDutyTimeMinutes(Math.max(0,
                safeMinutes(user.getTotalDutyTimeMinutes()) - getPlannedDutyDurationMinutes(duty) + getTrackedDutyDurationMinutes(assignment)));

        userRepository.save(user);
        assignmentRepository.save(assignment);
    }

    private CrewAssignment findAssignment(String login, Long dutyId) {
        User user = userRepository.findByLogin(login).orElseThrow();
        return user.getAssignments().stream()
                .filter(as -> as.getDuty().getId().equals(dutyId))
                .findFirst()
                .orElseThrow();
    }

    private int getPlannedDutyDurationMinutes(Duty duty) {
        if (duty.getDutyStartTime() == null || duty.getDutyEndTime() == null) {
            return 0;
        }
        return (int) Duration.between(duty.getDutyStartTime(), duty.getDutyEndTime()).toMinutes();
    }

    private int getPlannedWorkTimeMinutes(Duty duty) {
        return duty.getWorkTimeMinutes() != null ? duty.getWorkTimeMinutes() : 0;
    }

    private int getPlannedAirTimeMinutes(Duty duty) {
        return duty.getAirTimeMinutes() != null ? duty.getAirTimeMinutes() : 0;
    }

    private int getTrackedDutyDurationMinutes(CrewAssignment assignment) {
        if (assignment.getActualStartTime() != null && assignment.getActualEndTime() != null) {
            return (int) Duration.between(assignment.getActualStartTime(), assignment.getActualEndTime()).toMinutes();
        }
        return getPlannedDutyDurationMinutes(assignment.getDuty());
    }

    private int getTrackedWorkTimeMinutes(CrewAssignment assignment) {
        if (assignment.getActualStartTime() != null && assignment.getActualEndTime() != null) {
            int actualDutyMinutes = getTrackedDutyDurationMinutes(assignment);
            return Math.min(getPlannedWorkTimeMinutes(assignment.getDuty()), Math.max(0, actualDutyMinutes - 60));
        }
        return getPlannedWorkTimeMinutes(assignment.getDuty());
    }

    private int getTrackedAirTimeMinutes(CrewAssignment assignment) {
        if (assignment.getActualStartTime() != null && assignment.getActualEndTime() != null) {
            return Math.min(getPlannedAirTimeMinutes(assignment.getDuty()), getTrackedWorkTimeMinutes(assignment));
        }
        return getPlannedAirTimeMinutes(assignment.getDuty());
    }

    private int safeMinutes(Integer minutes) {
        return minutes != null ? minutes : 0;
    }

    private void recalculateDutyTimes(Duty duty, List<Flight> selectedFlights) {
        selectedFlights.sort(Comparator.comparing(Flight::getDepartureTime));

        Flight firstFlight = selectedFlights.get(0);
        Flight lastFlight = selectedFlights.get(selectedFlights.size() - 1);

        duty.setDutyStartTime(firstFlight.getDepartureTime().minusHours(1));
        duty.setDutyEndTime(lastFlight.getArrivalTime());
        duty.setAirTimeMinutes(selectedFlights.stream()
                .mapToInt(flight -> flight.getDurationMinutes() != null ? flight.getDurationMinutes() : 0)
                .sum());
        duty.setWorkTimeMinutes((int) Duration.between(firstFlight.getDepartureTime(), lastFlight.getArrivalTime()).toMinutes());
    }

    private AssignmentTimeSnapshot snapshotAssignmentTime(CrewAssignment assignment) {
        return new AssignmentTimeSnapshot(
                getTrackedAirTimeMinutes(assignment),
                getTrackedWorkTimeMinutes(assignment),
                getTrackedDutyDurationMinutes(assignment));
    }

    private record AssignmentTimeSnapshot(int airTimeMinutes, int workTimeMinutes, int dutyTimeMinutes) {
    }

    @Transactional
    public void acceptDuty(String login, Long dutyId) {
        CrewAssignment a = findAssignment(login, dutyId);
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

        int dutyDurationMinutes = getTrackedDutyDurationMinutes(a);

        user.setTotalAirBorneTimeMinutes(Math.max(0, safeMinutes(user.getTotalAirBorneTimeMinutes()) - getTrackedAirTimeMinutes(a)));
        user.setTotalWorkTimeMinutes(Math.max(0, safeMinutes(user.getTotalWorkTimeMinutes()) - getTrackedWorkTimeMinutes(a)));
        user.setTotalDutyTimeMinutes(Math.max(0, safeMinutes(user.getTotalDutyTimeMinutes()) - dutyDurationMinutes));

        userRepository.save(user);
        assignmentRepository.save(a);
    }

    @Transactional
    public void removeUserFromDuty(Long userId, Long dutyId) {
        User user = userRepository.findById(userId).orElseThrow();
        Duty duty = dutyRepository.findById(dutyId).orElseThrow();
        CrewAssignment a = user.getAssignments().stream()
                .filter(as -> as.getDuty().getId().equals(dutyId)).findFirst().orElseThrow();

        int dutyDurationMinutes = getTrackedDutyDurationMinutes(a);

        if (a.getStatus() != AssignmentStatus.REJECTED) {
            user.setTotalAirBorneTimeMinutes(Math.max(0, safeMinutes(user.getTotalAirBorneTimeMinutes()) - getTrackedAirTimeMinutes(a)));
            user.setTotalWorkTimeMinutes(Math.max(0, safeMinutes(user.getTotalWorkTimeMinutes()) - getTrackedWorkTimeMinutes(a)));
            user.setTotalDutyTimeMinutes(Math.max(0, safeMinutes(user.getTotalDutyTimeMinutes()) - dutyDurationMinutes));
        }

        user.getAssignments().remove(a);
        duty.getAssignments().remove(a);
        userRepository.save(user);
        dutyRepository.save(duty);
        assignmentRepository.delete(a);
    }
}
