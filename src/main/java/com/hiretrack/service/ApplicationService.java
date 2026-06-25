package com.hiretrack.service;

import com.hiretrack.dto.request.ApplicationRequest;
import com.hiretrack.dto.response.ApplicationResponse;
import com.hiretrack.exception.ConflictException;
import com.hiretrack.exception.ResourceNotFoundException;
import com.hiretrack.exception.UnauthorizedException;
import com.hiretrack.model.*;
import com.hiretrack.repository.ApplicationRepository;
import com.hiretrack.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final SkillMatcherService skillMatcherService;

    public List<ApplicationResponse> getAllApplication(User user) {
        return applicationRepository.findByUser(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ApplicationResponse getApplicationById(Long id, User user) {
        JobApplication app = findAndVerifyOwnership(id, user);
        return toResponse(app);
    }

    public ApplicationResponse createApplication(ApplicationRequest req, User user) {
        Job job = jobRepository.findById(req.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + req.getJobId()));

        applicationRepository.findByUserAndJob(user, job).ifPresent(existing -> {
            throw new ConflictException(
                    "You're already tracking this application (application id " + existing.getId() + ")");
        });

        JobApplication application = JobApplication.builder()
                .user(user)
                .job(job)
                .status(req.getStatus() != null ? req.getStatus() : ApplicationStatus.SAVED)
                .appliedDate(req.getAppliedDate())
                .followUpDate(req.getFollowUpDate())
                .notes(req.getNotes())
                .resumeVersion(req.getResumeVersion())
                .build();

        JobApplication saved = applicationRepository.save(application);

        skillMatcherService.calculateMatch(saved);

        saved = applicationRepository.findById(saved.getId()).orElse(saved);
        return toResponse(saved);
    }

    public ApplicationResponse updateApplication(Long id, ApplicationRequest req, User user) {
        JobApplication app = findAndVerifyOwnership(id, user);

        if (req.getStatus() != null)
            app.setStatus(req.getStatus());
        if (req.getAppliedDate() != null)
            app.setAppliedDate(req.getAppliedDate());
        if (req.getFollowUpDate() != null)
            app.setFollowUpDate(req.getFollowUpDate());
        if (req.getNotes() != null)
            app.setNotes(req.getNotes());
        if (req.getResumeVersion() != null)
            app.setResumeVersion(req.getResumeVersion());

        return toResponse(applicationRepository.save(app));
    }

    public void deleteApplication(long id, User user) {
        JobApplication app = findAndVerifyOwnership(id, user);
        applicationRepository.delete(app);
    }

    private JobApplication findAndVerifyOwnership(Long id, User user) {
        JobApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        if (!app.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You do not have permission to access this application");
        }

        return app;
    }

    private ApplicationResponse toResponse(JobApplication app) {
        Integer matchScore = null;
        if (app.getSkillMatch() != null) {
            matchScore = app.getSkillMatch().getMatchScore();
        }

        return ApplicationResponse.builder()
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