package com.tab.flight_crew_manager.user.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String login;
    private String password;
}
