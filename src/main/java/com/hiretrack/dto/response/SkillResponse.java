package com.hiretrack.dto.response;

import com.hiretrack.model.UserSkill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {
    private Long id;
    private String skillName;
    private UserSkill.Proficiency proficiency;
}