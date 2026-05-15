package com.HireTrack.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse{
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private String email;
    private String name;
}