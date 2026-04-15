package com.tab.flight_crew_manager.user;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {

    @Bean
    CommandLineRunner commandLineRunner(UserRepository repository) {
        return args -> {
            if (repository.findByLogin("admin_pilot").isEmpty()) {
                User admin = new User(
                        UserRole.ADMIN,
                        "admin_pilot",
                        "safePassword123",
                        "Jan Kowalski",
                        "+48 123 456 789"
                );
                repository.save(admin);
            }
        };
    }
}
