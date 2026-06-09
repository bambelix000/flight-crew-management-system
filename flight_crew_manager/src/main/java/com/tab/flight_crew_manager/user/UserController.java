package com.tab.flight_crew_manager.user;


import com.tab.flight_crew_manager.user.dto.PhoneUpdateDto;
import com.tab.flight_crew_manager.user.dto.StatsDataDto;
import com.tab.flight_crew_manager.user.dto.UserUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SCHEDULER')")
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
    public ResponseEntity<StatsDataDto> getMyStats(Principal principal) {

        StatsDataDto stats = userService.getStats(principal);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/my-report")
    public ResponseEntity<byte[]> getMyReport(Principal principal) {
        byte[] report = userService.generateMyReport(principal);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=flight-crew-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(report);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'SCHEDULER')")
    @GetMapping("/crew-stats")
    public ResponseEntity<List<StatsDataDto>> getAllCrewStats() {
        return ResponseEntity.ok(userService.getAllCrewStats());
    }
}
