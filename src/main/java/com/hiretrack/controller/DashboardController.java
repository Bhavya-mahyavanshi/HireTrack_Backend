package com.hiretrack.controller;

import com.hiretrack.dto.response.ApplicationResponse;
import com.hiretrack.dto.response.DashboardResponse;
import com.hiretrack.dto.response.StatusCount;
import com.hiretrack.model.User;
import com.hiretrack.repository.UserRepository;
import com.hiretrack.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<DashboardResponse> getStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(dashboardService.getStats(user));
    }

    @GetMapping("/funnel")
    public ResponseEntity<List<StatusCount>> getFunnel(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(dashboardService.getFunnel(user));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ApplicationResponse>> getUpcoming(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(dashboardService.getUpcoming(user));
    }

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}
