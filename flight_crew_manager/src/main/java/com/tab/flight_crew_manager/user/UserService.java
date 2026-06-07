package com.tab.flight_crew_manager.user;

import com.tab.flight_crew_manager.crew_assignment.AssignmentStatus;
import com.tab.flight_crew_manager.crew_assignment.CrewAssignment;
import com.tab.flight_crew_manager.duty.Duty;
import com.tab.flight_crew_manager.user.dto.StatsDataDto;
import com.tab.flight_crew_manager.user.dto.UserUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        if (userRepository.existsByLogin(user.getLogin())) {
            throw new IllegalStateException("Login jest już zajęty");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public StatsDataDto getStats(Principal principal) {
        String login = principal.getName();
        User user = userRepository.findByLogin(login).orElseThrow();
        LocalDateTime now = LocalDateTime.now();

        StatsDataDto dto = new StatsDataDto();
        dto.setId(user.getId());
        dto.setLogin(user.getLogin());     
        dto.setName(user.getName());
        dto.setSurname(user.getSurname());

        int rolling20Days = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .map(CrewAssignment::getDuty)
                .filter(d -> d.getDutyStartTime() != null && d.getDutyStartTime().isAfter(now.minusDays(20)))
                .mapToInt(Duty::getAirTimeMinutes).sum();

        int rolling365Days = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .map(CrewAssignment::getDuty)
                .filter(d -> d.getDutyStartTime() != null && d.getDutyStartTime().isAfter(now.minusDays(365)))
                .mapToInt(Duty::getAirTimeMinutes).sum();

        dto.setTwentyDaysAirTime(rolling20Days);
        dto.setAnnualAirTime(rolling365Days);

        dto.setTotalAirBorneTimeMinutes(user.getTotalAirBorneTimeMinutes() != null ? user.getTotalAirBorneTimeMinutes() : 0);
        dto.setTotalWorkTimeMinutes(user.getTotalWorkTimeMinutes() != null ? user.getTotalWorkTimeMinutes() : 0);
        dto.setTotalDutyTimeMinutes(user.getTotalDutyTimeMinutes() != null ? user.getTotalDutyTimeMinutes() : 0);
        dto.setIncapacityCounter(user.getIncapacityCounter());

        String topRole = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .map(a -> a.getRoleOnDuty().name())
                .reduce(java.util.function.BinaryOperator.maxBy((role1, role2) -> 1))
                .orElse("Brak lotów");
        dto.setMostFrequentRole(topRole);

        return dto;
    }

    public void updateUser(Long id, UserUpdateDto updatedData) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Użytkownik nie istnieje"));

        user.setName(updatedData.getName());
        user.setSurname(updatedData.getSurname());
        user.setLogin(updatedData.getLogin());
        user.setPhoneNumber(updatedData.getPhoneNumber());
        user.setUserRole(updatedData.getUserRole());
        userRepository.save(user);
    }

    public void updatePhone(Principal principal, String newPhone) {
        User user = userRepository.findByLogin(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Użytkownik nie istnieje"));
        user.setPhoneNumber(newPhone);
        userRepository.save(user);
    }

    public User login(String login, String password) {
        return userRepository.findByLogin(login)
                .filter(u -> u.getPassword().equals(password))
                .orElseThrow(() -> new IllegalStateException("Błędny login lub hasło"));
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Nie znaleziono użytkownika"));
    }

    public List<StatsDataDto> getAllCrewStats() {
        LocalDateTime now = LocalDateTime.now();

        return userRepository.findAll().stream()
                .filter(u -> u.getUserRole() == UserRole.CREWMEMBER)
                .map(user -> {
                    StatsDataDto dto = new StatsDataDto();
                    dto.setId(user.getId());
                    dto.setLogin(user.getLogin());
                    dto.setName(user.getName());
                    dto.setSurname(user.getSurname());

                    int rolling20Days = user.getAssignments().stream()
                            .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                            .map(CrewAssignment::getDuty)
                            .filter(d -> d.getDutyStartTime() != null && d.getDutyStartTime().isAfter(now.minusDays(20)))
                            .mapToInt(Duty::getAirTimeMinutes).sum();

                    int rolling365Days = user.getAssignments().stream()
                            .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                            .map(CrewAssignment::getDuty)
                            .filter(d -> d.getDutyStartTime() != null && d.getDutyStartTime().isAfter(now.minusDays(365)))
                            .mapToInt(Duty::getAirTimeMinutes).sum();

                    dto.setTwentyDaysAirTime(rolling20Days);
                    dto.setAnnualAirTime(rolling365Days);

                    dto.setTotalAirBorneTimeMinutes(user.getTotalAirBorneTimeMinutes() != null ? user.getTotalAirBorneTimeMinutes() : 0);
                    dto.setTotalWorkTimeMinutes(user.getTotalWorkTimeMinutes() != null ? user.getTotalWorkTimeMinutes() : 0);
                    dto.setTotalDutyTimeMinutes(user.getTotalDutyTimeMinutes() != null ? user.getTotalDutyTimeMinutes() : 0);
                    dto.setIncapacityCounter(user.getIncapacityCounter());

                    return dto;
                })
                .collect(Collectors.toList());
    }
}
