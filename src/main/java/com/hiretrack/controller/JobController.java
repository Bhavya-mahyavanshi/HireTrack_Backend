package com.hiretrack.controller;

import com.hiretrack.dto.request.ManualJobRequest;
import com.hiretrack.dto.request.ScrapeRequest;
import com.hiretrack.dto.response.JobResponse;
import com.hiretrack.exception.ResourceNotFoundException;
import com.hiretrack.model.Job;
import com.hiretrack.repository.JobRepository;
import com.hiretrack.service.JobScraperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobScraperService jobScraperService;
    private final JobRepository jobRepository;

    @PostMapping("/scrape")
    public ResponseEntity<JobResponse> scrape(@Valid @RequestBody ScrapeRequest req) {
        return ResponseEntity.ok(jobScraperService.scrapeJob(req.getUrl()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getById(@PathVariable Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + id));
        return ResponseEntity.ok(JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .requiredSkills(job.getRequiredSkills())
                .url(job.getUrl())
                .build());
    }

    // Used by the frontend's manual-entry fallback — fired when scraping
    // fails outright, or succeeds but returns placeholder "Unknown Title" /
    // "Unknown Company" values (Indeed/LinkedIn block scraping; some
    // Greenhouse/Lever pages have markup our selectors miss). Skips the
    // scraper entirely and saves exactly what the user typed.
    @PostMapping("/manual")
    public ResponseEntity<JobResponse> createManual(@Valid @RequestBody ManualJobRequest req) {
        Job job = Job.builder()
                .url(req.getUrl() != null && !req.getUrl().isBlank()
                        ? req.getUrl()
                        : "manual-entry-" + System.currentTimeMillis())
                .title(req.getTitle())
                .company(req.getCompany())
                .location(req.getLocation() != null ? req.getLocation() : "")
                .description("")
                .requiredSkills("")
                .build();

        Job saved = jobRepository.save(job);

        return ResponseEntity.ok(JobResponse.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .company(saved.getCompany())
                .location(saved.getLocation())
                .salaryMin(null)
                .salaryMax(null)
                .requiredSkills("")
                .url(saved.getUrl())
                .build());
    }
}