package com.tab.flight_crew_manager.duty.dto;

import com.tab.flight_crew_manager.flight.dto.FlightSummaryDto;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class DutyDto {
    private Long id;
    private LocalDateTime dutyStartTime;
    private LocalDateTime dutyEndTime;
    private Integer workTimeMinutes;
    private Integer airTimeMinutes;

    private List<FlightSummaryDto> flights;

    private List<CrewMemberDto> assignedCrew;
}