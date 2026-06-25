package com.hiretrack.dto.request;

import com.hiretrack.model.UserSkill;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SkillRequest {
    @NotBlank
    private String skillName;
    private UserSkill.Proficiency proficiency;
}
