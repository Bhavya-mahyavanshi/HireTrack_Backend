package com.hiretrack.dto.request;

import com.hiretrack.model.ApplicationStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ApplicationRequest {
    private Long jobId;
    private ApplicationStatus status;
    private LocalDate appliedDate;
    private LocalDate followUpDate;
    private String notes;
    private String resumeVersion;
}
