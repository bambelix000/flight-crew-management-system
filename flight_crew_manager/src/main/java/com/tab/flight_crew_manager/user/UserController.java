package com.tab.flight_crew_manager.user;


import com.tab.flight_crew_manager.user.dto.PhoneUpdateDto;
import com.tab.flight_crew_manager.user.dto.StatsData;
import com.tab.flight_crew_manager.user.dto.UserUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<Void> updateUser(@PathVariable Long id, @RequestBody UserUpdateDto updateDto) {
        userService.updateUser(id, updateDto);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update-phone")
    public ResponseEntity<Void> updateMyPhone(Principal principal, @RequestBody PhoneUpdateDto request) {
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        userService.updatePhone(principal, request.getPhoneNumber());
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
