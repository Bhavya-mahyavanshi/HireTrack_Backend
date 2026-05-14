package com.HireTrack.dto.request;

import com.HireTrack.model.UserSkill;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SkillRequest {
    @NotBlank
    private String skillName;
    private UserSkill.Proficiency proficiency;
}
