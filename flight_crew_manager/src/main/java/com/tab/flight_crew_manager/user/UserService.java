package com.tab.flight_crew_manager.user;

import com.tab.flight_crew_manager.crew_assignment.AssignmentStatus;
import com.tab.flight_crew_manager.crew_assignment.CrewAssignment;
import com.tab.flight_crew_manager.duty.Duty;
import com.tab.flight_crew_manager.user.dto.StatsData;
import com.tab.flight_crew_manager.user.dto.UserUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    public StatsData getStats(Principal principal) {
        String login = principal.getName();
        User user = userRepository.findByLogin(login).orElseThrow();
        LocalDateTime now = LocalDateTime.now();

        StatsData dto = new StatsData();

        dto.setId(user.getId());
        int rolling20Days = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .map(CrewAssignment::getDuty)
                .filter(d -> d.getDutyStartTime() != null && d.getDutyStartTime().isAfter(now.minusDays(20)))
                .mapToInt(Duty::getAirTimeMinutes)
                .sum();

        int rolling365Days = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .map(CrewAssignment::getDuty)
                .filter(d -> d.getDutyStartTime() != null && d.getDutyStartTime().isAfter(now.minusDays(365)))
                .mapToInt(Duty::getAirTimeMinutes)
                .sum();

        dto.setTwentyDaysAirTime(rolling20Days);
        dto.setAnnualAirTime(rolling365Days);

        long activeDutiesCount = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .count();
        dto.setTotalDutiesCount((int) activeDutiesCount);

        int totalWork = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .mapToInt(a -> a.getDuty().getWorkTimeMinutes())
                .sum();
        dto.setTotalWorkTimeMinutes(totalWork);

        String topRole = user.getAssignments().stream()
                .filter(a -> a.getStatus() != AssignmentStatus.REJECTED)
                .map(a -> a.getRoleOnDuty().name())
                .reduce(java.util.function.BinaryOperator.maxBy((role1, role2) -> 1))
                .orElse("Brak lotów");
        dto.setMostFrequentRole(topRole);

        dto.setIncapacityCounter(user.getIncapacityCounter());

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
}
