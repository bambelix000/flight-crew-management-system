package com.tab.flight_crew_manager.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

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

    public void updateUser(Long id, User updatedData) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Użytkownik nie istnieje"));
        user.setName(updatedData.getName());
        user.setSurname(updatedData.getSurname());
        user.setLogin(updatedData.getLogin());
        user.setPhoneNumber(updatedData.getPhoneNumber());
        user.setUserRole(updatedData.getUserRole());
        userRepository.save(user);
    }

    public void updatePhone(Long id, String newPhone) {
        User user = userRepository.findById(id)
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
