package com.tab.flight_crew_manager.user;

import com.tab.flight_crew_manager.user.dto.AuthResponse;
import com.tab.flight_crew_manager.security.JwtService;
import com.tab.flight_crew_manager.user.dto.LoginRequest;
import com.tab.flight_crew_manager.user.dto.StatsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @RequestBody User user) {
        userService.updateUser(id, user);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/phone")
    public ResponseEntity<Void> updateMyPhone(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newPhone = payload.get("phoneNumber");
        userService.updatePhone(id, newPhone);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/my-stats")
    public ResponseEntity<StatsData> getMyStats(Principal principal) {

        StatsData stats = userService.getStats(principal);

        return ResponseEntity.ok(stats);
    }
}
