package com.HireTrack.dto.response;

import com.HireTrack.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponce {
    private Long id;
    private String jobTitle;
    private String company;
    private String location;
    private ApplicationStatus status;
    private LocalDate appliedDate;
    private LocalDate followUpDate;
    private String notes;
    private String resumeVersion;
    private Integer matchScore;
}