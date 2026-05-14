package com.HireTrack.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScrapeRequest {
    @NotBlank
    private String url;
}
