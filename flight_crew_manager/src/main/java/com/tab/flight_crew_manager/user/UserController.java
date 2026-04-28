package com.tab.flight_crew_manager.user;

import com.tab.flight_crew_manager.user.dto.StatsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/my-stats")
    public ResponseEntity<StatsData> getMyStats(Principal principal) {

        StatsData stats = userService.getStats(principal);

        return ResponseEntity.ok(stats);
    }

}
