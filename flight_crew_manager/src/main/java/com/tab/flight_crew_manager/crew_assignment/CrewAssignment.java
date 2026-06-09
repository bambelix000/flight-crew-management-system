package com.tab.flight_crew_manager.crew_assignment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tab.flight_crew_manager.duty.Duty;
import com.tab.flight_crew_manager.duty.RoleOnDuty;
import com.tab.flight_crew_manager.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "crew_assignments")
@Getter
@Setter
@NoArgsConstructor
public class CrewAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assignment_seq")
    @SequenceGenerator(name = "assignment_seq", sequenceName = "assignment_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @ManyToOne
    @JoinColumn(name = "duty_id", nullable = false)
    @JsonIgnore
    private Duty duty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleOnDuty roleOnDuty;

    @Enumerated(EnumType.STRING)
    private AssignmentStatus status = AssignmentStatus.PENDING;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    public CrewAssignment(User user, Duty duty, RoleOnDuty roleOnDuty) {
        this.user = user;
        this.duty = duty;
        this.roleOnDuty = roleOnDuty;
    }
}
