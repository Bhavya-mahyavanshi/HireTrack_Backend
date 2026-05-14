package com.HireTrack.repository;

import com.HireTrack.model.ApplicationStatus;
import com.HireTrack.model.JobApplication;
import com.HireTrack.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByUser(User user);

    List<JobApplication> findByUserAndStatus(User user, ApplicationStatus status);

    List<JobApplication> findByFollowUpDateBefore(LocalDate date);

    Long countByUserAndStatus(User user, ApplicationStatus status);
}
