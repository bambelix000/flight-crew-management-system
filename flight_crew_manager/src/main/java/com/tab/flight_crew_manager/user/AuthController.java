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
        // 1. Spring Security automatycznie sprawdza, czy login i zahasłowane hasło się zgadzają
        // Jeśli coś jest nie tak, ta metoda rzuci wyjątek BadCredentialsException (status 403/401)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getLogin(),
                        loginRequest.getPassword()
                )
        );

        // 2. Jeśli doszliśmy tutaj, to znaczy że hasło jest na 100% poprawne.
        // Pobieramy użytkownika z bazy, żeby mieć dostęp do jego danych (np. Roli)
        User user = userRepository.findByLogin(loginRequest.getLogin())
                .orElseThrow();

        // 3. Konwertujemy naszego Usera na UserDetails (tak jak zrobiliśmy w CustomUserDetailsService)
        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getLogin())
                        .password(user.getPassword())
                        .authorities(user.getUserRole().name())
                        .build();

        // 4. Generujemy token JWT
        String jwtToken = jwtService.generateToken(userDetails);

        // 5. Zwracamy piękny obiekt JSON dla frontendu
        return ResponseEntity.ok(new AuthResponse(
                jwtToken,
                user.getUserRole().name(),
                user.getLogin()
        ));
    }
}