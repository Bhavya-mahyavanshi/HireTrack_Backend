package com.hiretrack.service;

import com.hiretrack.model.JobApplication;
import com.hiretrack.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailReminderService {

    private final JavaMailSender mailSender;
    private final ApplicationRepository applicationRepository;

    public void sendFollowUpReminders() {
        LocalDate today = LocalDate.now();
        List<JobApplication> dueApplications = applicationRepository.findByFollowUpDate(today);

        int count = 0;
        for (JobApplication app : dueApplications) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(app.getUser().getEmail());
                message.setSubject("hiretrack: Follow up on " + app.getJob().getCompany());
                message.setText(buildReminderText(app));
                mailSender.send(message);
                count++;
            } catch (Exception e) {
                log.error("Failed to send reminder email for application {} : {}",
                        app.getId(), e.getMessage());
            }
        }
        log.info("hiretrack Scheduler: sent {} follow-up reminder(s)", count);
    }

    private String buildReminderText(JobApplication app) {
        return String.format(
                "Hi %s,\n\n" +
                        "This is a reminder from hiretrack to follow up on your job application.\n\n" +
                        "Company:   %s\n" +
                        "Role:      %s\n" +
                        "Status:    %s\n" +
                        "Follow-up date: %s\n\n" +
                        "Log in to hiretrack to update your application status.\n\n" +
                        "Good luck!\n" +
                        "- The hiretrack Team",
                app.getUser().getName(),
                app.getJob().getCompany(),
                app.getJob().getTitle(),
                app.getStatus(),
                app.getFollowUpDate()
        );
    }
}