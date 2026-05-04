package com.tab.flight_crew_manager.duty.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrewMemberDto {
    private Long userId;
    private String name;
    private String surname;
    private String roleOnDuty;
    private String status;
    private String rejectionReason;
}