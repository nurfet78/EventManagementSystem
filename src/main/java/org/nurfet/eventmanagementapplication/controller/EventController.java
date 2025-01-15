package org.nurfet.eventmanagementapplication.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.eventmanagementapplication.dto.EventDTO;
import org.nurfet.eventmanagementapplication.dto.EventRegistrationDTO;
import org.nurfet.eventmanagementapplication.dto.ParticipantDTO;
import org.nurfet.eventmanagementapplication.service.EventService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventDTO>> getAllEvents() {
        return ResponseEntity.ok(eventService.getAllEvents());
    }

    @PostMapping
    public ResponseEntity<EventDTO> createEvent(@Valid @RequestBody EventDTO eventDTO) {
        return ResponseEntity.ok(eventService.createEvent(eventDTO));
    }

    @GetMapping("/between")
    public ResponseEntity<List<EventDTO>> getEventsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(eventService.getEventsBetweenDates(start, end));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Мероприятие с идентификатором %d успешно удалено", id));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{eventId}/register")
    public ResponseEntity<Map<String, String>> registerParticipant(
            @PathVariable Long eventId,
            @Valid @RequestBody EventRegistrationDTO registrationDTO) {
        ParticipantDTO participant = eventService.registerParticipant(eventId, registrationDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Участник %s %s успешно зарегистрирован на мероприятии с идентификатором %d",
                participant.getFirstName(), participant.getLastName(), eventId));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventDTO eventDTO) {
        EventDTO updated = eventService.updateEvent(id, eventDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Мероприятие с идентификатором %d успешно обновлено", id));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{eventId}/participants")
    public ResponseEntity<Object> getEventParticipants(@PathVariable Long eventId) {
        List<ParticipantDTO> participants = eventService.getEventParticipants(eventId);

        if (participants.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "На данное мероприятие ещё нет зарегистрированных участников");
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.ok(participants);
    }
}
