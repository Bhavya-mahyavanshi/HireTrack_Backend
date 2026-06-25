package com.HireTrack.controller;

import com.HireTrack.dto.request.SkillRequest;
import com.HireTrack.dto.response.SkillMatchResponse;
import com.HireTrack.dto.response.SkillResponse;
import com.HireTrack.exception.ResourceNotFoundException;
import com.HireTrack.exception.UnauthorizedException;
import com.HireTrack.model.JobApplication;
import com.HireTrack.model.SkillMatch;
import com.HireTrack.model.User;
import com.HireTrack.model.UserSkill;
import com.HireTrack.repository.ApplicationRepository;
import com.HireTrack.repository.SkillRepository;
import com.HireTrack.repository.UserRepository;
import com.HireTrack.service.SkillMatcherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final SkillMatcherService skillMatcherService;

    @GetMapping
    public ResponseEntity<List<SkillResponse>> getSkills(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        List<SkillResponse> skills = skillRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(skills);
    }

    @PostMapping
    public ResponseEntity<SkillResponse> addSkill(
            @Valid @RequestBody SkillRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);

        UserSkill skill = UserSkill.builder()
                .user(user)
                .skillName(req.getSkillName())
                .proficiency(req.getProficiency())
                .build();

        UserSkill saved = skillRepository.save(skill);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);

        UserSkill skill = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found with id: " + id));

        if (!skill.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not own this skill");
        }

        skillRepository.delete(skill);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/match/{appId}")
    public ResponseEntity<SkillMatchResponse> recalculateMatch(
            @PathVariable Long appId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);

        JobApplication app = applicationRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + appId));

        if (!app.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have permission to access this application");
        }

        SkillMatch match = skillMatcherService.calculateMatch(app);

        List<String> matched = match.getMatchedSkills() != null && !match.getMatchedSkills().isBlank()
                ? Arrays.asList(match.getMatchedSkills().split(","))
                : Collections.emptyList();

        List<String> missing = match.getMissingSkills() != null && !match.getMissingSkills().isBlank()
                ? Arrays.asList(match.getMissingSkills().split(","))
                : Collections.emptyList();

        return ResponseEntity.ok(SkillMatchResponse.builder()
                .matchScore(match.getMatchScore())
                .matchedSkills(matched)
                .missingSkills(missing)
                .build());
    }

    private SkillResponse toResponse(UserSkill skill) {
        return SkillResponse.builder()
                .id(skill.getId())
                .skillName(skill.getSkillName())
                .proficiency(skill.getProficiency())
                .build();
    }

    private User getUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }
}