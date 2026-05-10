package com.tab.flight_crew_manager.user.dto;

import lombok.Data;

@Data
public class StatsData {
    private Long id;
    private String name;
    private String surname;
    private String phoneNumber;
    private String mostFrequentRole;

    private Integer annualAirTime;
    private Integer twentyDaysAirTime;
    private Integer totalWorkTimeMinutes;
    private Integer totalDutyTimeMinutes;
    private Integer totalAirBorneTimeMinutes;
    private int incapacityCounter;
    private int totalDutiesCount;
}
