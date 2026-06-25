package com.hiretrack.repository;

import com.hiretrack.model.User;
import com.hiretrack.model.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<UserSkill, Long> {
    List<UserSkill> findByUser(User user);

    Optional<UserSkill> findByUserAndSkillNameIgnoreCase(User user, String skillName);

    void deleteByUserAndSkillName(User user, String skillName);
} 
