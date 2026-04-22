package com.tab.flight_crew_manager.user;

import com.tab.flight_crew_manager.user.dto.AuthResponse;
import com.tab.flight_crew_manager.security.JwtService;
import com.tab.flight_crew_manager.user.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;



}
