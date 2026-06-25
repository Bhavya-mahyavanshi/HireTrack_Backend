package com.hiretrack.repository;

import com.hiretrack.model.ApplicationStatus;
import com.hiretrack.model.Job;
import com.hiretrack.model.JobApplication;
import com.hiretrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByUser(User user);

    List<JobApplication> findByUserAndStatus(User user, ApplicationStatus status);

    List<JobApplication> findByFollowUpDateBefore(LocalDate date);

    List<JobApplication> findByFollowUpDate(LocalDate date);

    Optional<JobApplication> findByUserAndJob(User user, Job job);

    Long countByUserAndStatus(User user, ApplicationStatus status);
}