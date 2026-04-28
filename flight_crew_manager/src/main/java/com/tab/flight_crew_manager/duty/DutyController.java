package com.tab.flight_crew_manager.duty;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/duties")
@RequiredArgsConstructor
public class DutyController {

    private final DutyService dutyService;

    @PostMapping("/create-from-flights")
    public ResponseEntity<Duty> createDuty(@RequestBody List<Long> flightIds) {
        Duty createdDuty = dutyService.createDutyFromFlights(flightIds);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDuty);
    }

    @PostMapping("/{dutyId}/assign/{userId}")
    public ResponseEntity<String> assignUserToDuty(
            @PathVariable Long dutyId,
            @PathVariable Long userId,
            @RequestParam RoleOnDuty role) {

        try {
            dutyService.assignUserToDuty(userId, dutyId, role);
            return ResponseEntity.ok("Użytkownik został pomyślnie przypisany do służby.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}