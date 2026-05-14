package com.HireTrack.model;

import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties.Apiversion.Use;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_skills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSkill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    private String skillName;

    @Enumerated(EnumType.STRING)
    private Proficiency Proficiency;

    public enum Proficiency {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}
