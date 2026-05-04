package com.tab.flight_crew_manager.duty;

import com.tab.flight_crew_manager.duty.dto.DutyAssignmentDto;
import com.tab.flight_crew_manager.duty.dto.DutyDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
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

    @PostMapping("/{dutyId}/assign")
    public ResponseEntity<?> assignUserToDuty(
            @PathVariable Long dutyId,
            @RequestBody DutyAssignmentDto request) {

        try {
            dutyService.assignUserToDuty(request.getUserId(), dutyId, request.getRole());
            return ResponseEntity.ok().build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{dutyId}/accept")
    public ResponseEntity<?> acceptDuty(@PathVariable Long dutyId, Principal principal) {
        try {
            dutyService.acceptDuty(principal.getName(), dutyId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{dutyId}/reject")
    public ResponseEntity<?> reportIncapacity(@PathVariable Long dutyId, Principal principal) {
        try {
            dutyService.reportIncapacity(principal.getName(), dutyId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/my-duties")
    public ResponseEntity<List<DutyDto>> getMyDuties(Principal principal) {
        String login = principal.getName();
        return ResponseEntity.ok(dutyService.getMyDuties(login));
    }

    @PreAuthorize("hasAuthority('SCHEDULER') or hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<List<DutyDto>> getAllDuties() {
        return ResponseEntity.ok(dutyService.getAllDuties());
    }

    @DeleteMapping("/{dutyId}/crew/{userId}")
    public ResponseEntity<?> removeUserFromDuty(@PathVariable Long dutyId, @PathVariable Long userId) {
        try {
            dutyService.removeUserFromDuty(userId, dutyId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}