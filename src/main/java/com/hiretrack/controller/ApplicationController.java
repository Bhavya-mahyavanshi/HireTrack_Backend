package com.HireTrack.controller;

import com.HireTrack.dto.request.ApplicationRequest;
import com.HireTrack.dto.response.ApplicationResponse;
import com.HireTrack.model.User;
import com.HireTrack.repository.UserRepository;
import com.HireTrack.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(applicationService.getAllApplication(user));
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(
            @Valid @RequestBody ApplicationRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(applicationService.createApplication(req, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(applicationService.getApplicationById(id, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> update (
        @PathVariable Long id,
        @RequestBody ApplicationRequest req,
        @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        return ResponseEntity.ok(applicationService.updateApplication(id, req, user));     
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable Long id,
        @AuthenticationPrincipal UserDetails userDetails){
            User user = getUser(userDetails);
            applicationService.deleteApplication(id, user);
            return ResponseEntity.noContent().build();
    }

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

}