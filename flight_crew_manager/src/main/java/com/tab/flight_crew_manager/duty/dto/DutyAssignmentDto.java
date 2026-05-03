package com.tab.flight_crew_manager.duty.dto;

import com.tab.flight_crew_manager.duty.RoleOnDuty;
import lombok.Data;

@Data
public class DutyAssignmentDto {
    private Long userId;
    private RoleOnDuty role;
}