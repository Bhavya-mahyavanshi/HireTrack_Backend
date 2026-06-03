package com.HireTrack.service;

import com.HireTrack.dto.response.ApplicationResponce;
import com.HireTrack.dto.response.DashboardResponse;
import com.HireTrack.dto.response.StatusCount;
import com.HireTrack.model.ApplicationStatus;
import com.HireTrack.model.JobApplication;
import com.HireTrack.model.User;
import com.HireTrack.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ApplicationRepository applicationRepository;

    public DashboardResponse getStats(User user) {
        int totalApplied = count(user, ApplicationStatus.APPLIED);
        int totalOffers = count(user, ApplicationStatus.OFFER);
        int totalRejected = count(user, ApplicationStatus.REJECTED);

        int totalInterviews = count(user, ApplicationStatus.PHONE_SCREEN)
                + count(user, ApplicationStatus.TECHNICAL)
                + count(user, ApplicationStatus.FINAL_ROUND);

        List<StatusCount> statusBreakdown = Arrays.stream(ApplicationStatus.values())
                .map(status -> StatusCount.builder()
                        .status(status)
                        .count(applicationRepository.countByUserAndStatus(user, status))
                        .build())
                .collect(Collectors.toList());

        LocalDate cutoff = LocalDate.now().plusDays(7);
        List<ApplicationResponce> upcomingFollowUps = applicationRepository.findByFollowUpDateBefore(cutoff)
                .stream()
                .filter(app -> app.getUser().getId().equals(user.getId()))
                .filter(app -> app.getFollowUpDate() != null
                        && !app.getFollowUpDate().isBefore(LocalDate.now()))
                .map(this::toResponse)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalApplied(totalApplied)
                .totalInterviews(totalInterviews)
                .totalOffers(totalOffers)
                .totalRejected(totalRejected)
                .statusBreakdown(statusBreakdown)
                .upcomingFollowUps(upcomingFollowUps)
                .build();
    }
    
    public List<StatusCount> getFunnel(User user) {
        return Arrays.stream(ApplicationStatus.values())
                .map(status -> StatusCount.builder()
                        .status(status)
                        .count(applicationRepository.countByUserAndStatus(user, status))
                        .build())
                .collect(Collectors.toList());
    }
    
    public List<ApplicationResponce> getUpcoming(User user) {
        LocalDate cutoff = LocalDate.now().plusDays(7);
        return applicationRepository.findByFollowUpDateBefore(cutoff)
                .stream()
                .filter(app -> app.getUser().getId().equals(user.getId()))
                .filter(app -> app.getFollowUpDate() != null
                        && !app.getFollowUpDate().isBefore(LocalDate.now()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private int count(User user, ApplicationStatus status) {
        Long c = applicationRepository.countByUserAndStatus(user, status);
        return c == null ? 0 : c.intValue();
    }

    private ApplicationResponce toResponse(JobApplication app) {
        Integer matchScore = app.getSkillMatch() != null ? app.getSkillMatch().getMatchScore() : null;
        return ApplicationResponce.builder()
                .id(app.getId())
                .jobTitle(app.getJob().getTitle())
                .company(app.getJob().getCompany())
                .location(app.getJob().getLocation())
                .status(app.getStatus())
                .appliedDate(app.getAppliedDate())
                .followUpDate(app.getFollowUpDate())
                .notes(app.getNotes())
                .resumeVersion(app.getResumeVersion())
                .matchScore(matchScore)
                .build();
    }
    
}
