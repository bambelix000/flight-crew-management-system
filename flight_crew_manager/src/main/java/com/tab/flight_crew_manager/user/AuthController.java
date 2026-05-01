package com.tab.flight_crew_manager.user;

import com.tab.flight_crew_manager.user.dto.AuthResponse;
import com.tab.flight_crew_manager.security.JwtService;
import com.tab.flight_crew_manager.user.dto.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;


    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.registerUser(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getLogin(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByLogin(loginRequest.getLogin())
                .orElseThrow();

        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getLogin())
                        .password(user.getPassword())
                        .authorities(user.getUserRole().name())
                        .build();

        String jwtToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(
        jwtToken,
        user.getUserRole().name(),
        user.getLogin()
        ));
    }
}