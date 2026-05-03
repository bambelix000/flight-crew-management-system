package com.tab.flight_crew_manager.duty.dto;

import com.tab.flight_crew_manager.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CrewMemberDto {
    private Long userId;
    private String name;
    private String surname;
    private String roleOnDuty;
    private String status;
}