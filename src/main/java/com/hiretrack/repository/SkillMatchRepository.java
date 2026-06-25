package com.hiretrack.repository;

import com.hiretrack.model.JobApplication;
import com.hiretrack.model.SkillMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillMatchRepository extends JpaRepository<SkillMatch, Long> {

    Optional<SkillMatch> findByApplication(JobApplication application);
} 
