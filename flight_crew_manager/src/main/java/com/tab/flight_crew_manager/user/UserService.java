package com.tab.flight_crew_manager.user;

import com.tab.flight_crew_manager.user.dto.StatsData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
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
        String userLogin = principal.getName();

        Optional<User> userOptional = userRepository.findByLogin(userLogin);

        if(userOptional.isEmpty()){
            throw new IllegalArgumentException("nie znany user");
        }
        User user = userOptional.get();

        StatsData stats = new StatsData();
        stats.setAnnualAirTime(user.getAnnualAirTime());
        stats.setTotalDutyTimeMinutes(user.getTotalDutyTimeMinutes());
        stats.setTotalAirBorneTimeMinutes(user.getTotalAirBorneTimeMinutes());
        stats.setTotalWorkTimeMinutes(user.getTotalWorkTimeMinutes());
        stats.setTwentyDaysAirTime(user.getTwentyDaysAirTime());

        return stats;
    }


}
