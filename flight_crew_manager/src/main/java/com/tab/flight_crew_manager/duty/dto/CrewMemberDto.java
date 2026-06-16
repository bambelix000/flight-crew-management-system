package com.tab.flight_crew_manager.duty.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrewMemberDto {
    private Long userId;
    private String login;
    private String name;
    private String surname;
    private String phoneNumber;
    private String roleOnDuty;
    private String status;
    private String rejectionReason;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
}
