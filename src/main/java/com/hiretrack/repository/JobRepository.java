package com.HireTrack.repository;

import com.HireTrack.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findByUrl(String url);

    List<Job> findByCompanyContainingIgnoreCase(String company);
} 
