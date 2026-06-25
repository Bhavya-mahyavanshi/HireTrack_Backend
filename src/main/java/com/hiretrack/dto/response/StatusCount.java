package com.hiretrack.dto.response;

import com.hiretrack.model.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusCount {
    private ApplicationStatus status;
    private Long count;
    
}
