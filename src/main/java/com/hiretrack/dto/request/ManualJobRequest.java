package com.hiretrack.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ManualJobRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String company;

    private String location;

    private String url;
}