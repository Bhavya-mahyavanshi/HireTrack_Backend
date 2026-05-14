package com.HireTrack.repository;

import com.HireTrack.model.User;
import com.HireTrack.model.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findByUser(User user);

    Optional<UserSkill> findByUserAndSkillNameIgnoreCase(User user, String skillName);

    void dedeleteByUserAndSkillName(User user, String skillName);
} 
