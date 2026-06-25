package com.HireTrack.model;

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
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String skillName;

    @Enumerated(EnumType.STRING)
    private Proficiency proficiency;

    public enum Proficiency {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}