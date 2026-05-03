package com.tab.flight_crew_manager.user.dto;

import com.tab.flight_crew_manager.user.UserRole;
import lombok.Data;

@Data
public class UserUpdateDto {
    private String name;
    private String surname;
    private String login;
    private String phoneNumber;
    private UserRole userRole;
}