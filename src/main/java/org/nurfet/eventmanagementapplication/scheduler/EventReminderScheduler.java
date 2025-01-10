package org.nurfet.eventmanagementapplication.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.eventmanagementapplication.model.Event;
import org.nurfet.eventmanagementapplication.model.Participant;
import org.nurfet.eventmanagementapplication.repository.EventRepository;
import org.nurfet.eventmanagementapplication.service.EmailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventReminderScheduler {

    private final EventRepository eventRepository;
    private final EmailService emailService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Scheduled(cron = "0 0 8 * * *") // Runs at 8:00 AM every day
    public void sendEventReminders() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(0).withMinute(0);
        LocalDateTime end = start.plusDays(1);

        List<Event> upcomingEvents = eventRepository.findEventsBetweenDates(start, end);

        for (Event event : upcomingEvents) {
            for (Participant participant : event.getParticipants()) {
                try {
                    emailService.sendEventReminder(
                            participant.getEmail(),
                            event.getName(),
                            event.getStartTime().format(formatter)
                    );
                    log.info("Напоминание о событии {} отправлено участнику {}", event.getName(), participant.getEmail());
                } catch (Exception e) {
                    log.error("Не удалось отправить напоминание о событии {} участнику {}",
                            event.getName(), participant.getEmail(), e);
                }
            }
        }
    }
}
