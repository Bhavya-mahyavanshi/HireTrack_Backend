package com.hiretrack.controller;

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
}
