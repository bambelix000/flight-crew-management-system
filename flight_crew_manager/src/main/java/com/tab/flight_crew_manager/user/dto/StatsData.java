package com.tab.flight_crew_manager.user.dto;

import lombok.Data;

@Data
public class StatsData {
    private String name;
    private String surname;
    private String phoneNumber;

    private Integer annualAirTime;
    private Integer twentyDaysAirTime;
    private Integer totalWorkTimeMinutes;
    private Integer totalDutyTimeMinutes;
    private Integer totalAirBorneTimeMinutes;
}
