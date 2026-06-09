package com.tab.flight_crew_manager.duty;

import com.tab.flight_crew_manager.duty.dto.ActionRequestDto;
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

    @PreAuthorize("hasAuthority('SCHEDULER') or hasAuthority('ADMIN')")
    @PostMapping("/create-from-flights")
    public ResponseEntity<?> createDuty(@RequestBody List<Long> flightIds) {
        try {
            Duty createdDuty = dutyService.createDutyFromFlights(flightIds);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDuty);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{dutyId}/assign")
    public ResponseEntity<String> assignUserToDuty(@PathVariable Long dutyId, @RequestBody DutyAssignmentDto request) {
        try {
            String result = dutyService.assignUserToDuty(request.getUserId(), dutyId, request.getRole());
            return ResponseEntity.ok(result);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{dutyId}/accept")
    public ResponseEntity<Void> acceptDuty(@PathVariable Long dutyId, Principal principal) {
        dutyService.acceptDuty(principal.getName(), dutyId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{dutyId}/reject")
    public ResponseEntity<Void> rejectDuty(@PathVariable Long dutyId, @RequestBody ActionRequestDto request, Principal principal) {
        dutyService.rejectDuty(principal.getName(), dutyId, request.getReason());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{dutyId}/start")
    public ResponseEntity<String> startDuty(@PathVariable Long dutyId, Principal principal) {
        try {
            dutyService.startDuty(principal.getName(), dutyId);
            return ResponseEntity.ok("STARTED");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{dutyId}/stop")
    public ResponseEntity<String> stopDuty(@PathVariable Long dutyId, Principal principal) {
        try {
            dutyService.stopDuty(principal.getName(), dutyId);
            return ResponseEntity.ok("STOPPED");
        } catch (IllegalStateException | IllegalArgumentException e) {
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
    public ResponseEntity<Void> removeUserFromDuty(@PathVariable Long dutyId, @PathVariable Long userId) {
        dutyService.removeUserFromDuty(userId, dutyId);
        return ResponseEntity.ok().build();
    }
}
