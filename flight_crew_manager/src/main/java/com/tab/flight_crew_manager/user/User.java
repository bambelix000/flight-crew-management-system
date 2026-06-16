package com.tab.flight_crew_manager.user;

import com.tab.flight_crew_manager.crew_assignment.CrewAssignment;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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
    @Enumerated(EnumType.STRING)
    private UserRole userRole;
    private String login;
    private String password;
    private String name;
    private String surname;
    private String phoneNumber;
    private Integer annualAirTime;      //Limit: 54000min (900h)
    private Integer twentyDaysAirTime;  //Limit: 5400 min (90h)
    private Integer totalWorkTimeMinutes;
    private Integer totalDutyTimeMinutes;
    private Integer totalAirBorneTimeMinutes;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrewAssignment> assignments = new ArrayList<>();

    @Column(name = "incapacity_counter")
    private int incapacityCounter = 0;

    public User(UserRole userRole, String login, String password, String name, String surname, String phoneNumber) {
        this.userRole = userRole;
        this.login = login;
        this.password = password;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.annualAirTime = 0;
        this.twentyDaysAirTime = 0;
        this.totalWorkTimeMinutes = 0;
        this.totalDutyTimeMinutes = 0;
        this.totalAirBorneTimeMinutes = 0;
    }

}
