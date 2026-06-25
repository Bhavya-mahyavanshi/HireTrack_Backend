package com.hiretrack.scheduler;

import com.hiretrack.service.EmailReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {
    
    private final EmailReminderService emailReminderService;

    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyReminders() {
        log.info("ReminderScheduler: starting daily follow-up reminder job");
        emailReminderService.sendFollowUpReminders();
        log.info("ReminderScheduler: daily follow-up reminder job complete");
    }
}
