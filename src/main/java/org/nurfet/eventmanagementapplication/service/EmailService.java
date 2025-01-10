package org.nurfet.eventmanagementapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendEventReminder(String to, String eventName, String eventDateTime) {
        String subject = "Напоминание: предстоящее событие";
        String text = String.format("""
                        Уважаемый участник,
                        
                        Напоминаем, что мероприятие '%s' \
                        запланировано на %s.
                        
                        С уважением,
                        Система управления мероприятиями""",
                eventName, eventDateTime);
        sendEmail(to, subject, text);
    }
}
