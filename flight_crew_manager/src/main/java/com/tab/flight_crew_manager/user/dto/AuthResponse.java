package com.tab.flight_crew_manager.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String role;
    private String login;
    private Long id;
    private String name;
    private String surname;
    private String phoneNumber;
}