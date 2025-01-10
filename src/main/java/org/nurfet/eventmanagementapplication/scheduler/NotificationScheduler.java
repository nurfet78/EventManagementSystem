package org.nurfet.eventmanagementapplication.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.eventmanagementapplication.model.Event;
import org.nurfet.eventmanagementapplication.model.Participant;
import org.nurfet.eventmanagementapplication.service.EmailService;
import org.nurfet.eventmanagementapplication.service.EventService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final EventService eventService;
    private final EmailService emailService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Scheduled(cron = "0 0 8 * * *") // Запуск каждый день в 8:00
    public void sendNotifications() {
        log.info("Запуск процесса отправки уведомлений");
        List<Event> upcomingEvents = eventService.getUpcomingEvents();

        for (Event event : upcomingEvents) {
            for (Participant participant : event.getParticipants()) {
                try {
                    emailService.sendEventReminder(
                            participant.getEmail(),
                            event.getName(),
                            event.getStartTime().format(formatter)
                    );
                    log.info("Отправлено напоминание о событии {} участнику {}",
                            event.getName(), participant.getEmail());
                } catch (Exception e) {
                    log.error("Не удалось отправить напоминание о событии {} участнику {}: {}",
                            event.getName(), participant.getEmail(), e.getMessage());
                }
            }
        }
        log.info("Завершен процесс отправки уведомления");
    }
}
