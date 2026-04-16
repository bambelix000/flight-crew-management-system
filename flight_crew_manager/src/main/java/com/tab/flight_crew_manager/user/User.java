package com.tab.flight_crew_manager.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1)
    private Long id;
    private UserRole userRole;
    private String login;
    private String password;
    private String fullName;
    private String phoneNumber;

    public User(UserRole userRole, String login, String password, String fullName, String phoneNumber) {
        this.userRole = userRole;
        this.login = login;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
    }

}
