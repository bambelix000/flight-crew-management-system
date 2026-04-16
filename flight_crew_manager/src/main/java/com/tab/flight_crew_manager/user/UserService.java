package com.tab.flight_crew_manager.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(User user) {
        if (userRepository.existsByLogin(user.getLogin())) {
            throw new IllegalStateException("Login jest już zajęty");
        }
        return userRepository.save(user);
    }

    public User login(String login, String password) {
        return userRepository.findByLogin(login)
                .filter(u -> u.getPassword().equals(password))
                .orElseThrow(() -> new IllegalStateException("Błędny login lub hasło"));
    }
}
