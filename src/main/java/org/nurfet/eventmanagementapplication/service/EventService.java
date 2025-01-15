package org.nurfet.eventmanagementapplication.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nurfet.eventmanagementapplication.dto.EventDTO;
import org.nurfet.eventmanagementapplication.dto.EventRegistrationDTO;
import org.nurfet.eventmanagementapplication.dto.ParticipantDTO;
import org.nurfet.eventmanagementapplication.exception.ResourceNotFoundException;
import org.nurfet.eventmanagementapplication.model.Event;
import org.nurfet.eventmanagementapplication.model.Participant;
import org.nurfet.eventmanagementapplication.model.Room;
import org.nurfet.eventmanagementapplication.repository.EventRepository;
import org.nurfet.eventmanagementapplication.repository.ParticipantRepository;
import org.nurfet.eventmanagementapplication.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.nurfet.eventmanagementapplication.service.ParticipantService.getParticipantDTO;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;

    public List<EventDTO> getAllEvents() {
        return eventRepository.findAllByDeletedFalse().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EventDTO createEvent(EventDTO dto) {
        validateEventTimes(dto);
        Room room = getRoomById(dto.getRoomId());
        validateRoomAvailability(null, room.getId(), dto.getStartTime(), dto.getEndTime());

        Event event = new Event();
        updateEventFields(event, dto, room);
        event = eventRepository.save(event);
        return convertToDTO(event);
    }

    @Transactional
    public EventDTO updateEvent(Long id, EventDTO dto) {
        Event event = getEventEntityById(id);
        Room room = getRoomById(dto.getRoomId());

        validateEventTimes(dto);
        validateRoomAvailability(id, room.getId(), dto.getStartTime(), dto.getEndTime());

        updateEventFields(event, dto, room);
        event = eventRepository.save(event);
        return convertToDTO(event);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = getEventEntityById(id);
        event.setDeleted(true);
        eventRepository.save(event);
    }

    public EventDTO getEvent(Long id) {
        return convertToDTO(getEventEntityById(id));
    }

    public List<ParticipantDTO> getEventParticipants(Long eventId) {
        Event event = getEventEntityById(eventId);
        return event.getParticipants().stream()
                .filter(participant -> !participant.isDeleted())
                .map(this::convertToParticipantDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ParticipantDTO registerParticipant(Long eventId, EventRegistrationDTO registrationDTO) {
        Event event = getEventEntityById(eventId);

        if (event.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Невозможно зарегистрироваться на прошедшее мероприятие");
        }

        Participant participant = getOrCreateParticipant(registrationDTO);

        if (event.getParticipants().contains(participant)) {
            throw new IllegalStateException("Участник уже зарегистрирован на это мероприятие");
        }

        event.getParticipants().add(participant);
        eventRepository.save(event);
        return convertToParticipantDTO(participant);
    }

    private void validateEventTimes(EventDTO dto) {
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new IllegalArgumentException("Время окончания не может быть раньше времени начала");
        }

        if (dto.getStartTime().isBefore(LocalDateTime.now().toLocalDate().atStartOfDay())) {
            throw new IllegalArgumentException("Дата начала не может быть в прошлом");
        }
    }

    private void validateRoomAvailability(Long eventId, Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Event> eventsInRoom = eventRepository.findByRoomIdAndDeletedFalse(roomId);

        boolean isOccupied = eventsInRoom.stream()
                .filter(event -> eventId == null || !event.getId().equals(eventId)) // Изменили условие
                .anyMatch(event ->
                        (startTime.isBefore(event.getEndTime()) || startTime.equals(event.getEndTime())) &&
                                (endTime.isAfter(event.getStartTime()) || endTime.equals(event.getStartTime()))
                );

        if (isOccupied) {
            throw new IllegalStateException("Помещение уже забронировано на это время");
        }
    }

    private void updateEventFields(Event event, EventDTO dto, Room room) {
        event.setName(dto.getName());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setRoom(room);
    }

    private Room getRoomById(Long roomId) {
        return roomRepository.findByIdAndDeletedFalse(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Помещение не найдено"));
    }

    private Event getEventEntityById(Long id) {
        return eventRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Событие не найдено"));
    }

    private Participant getOrCreateParticipant(EventRegistrationDTO dto) {
        return participantRepository.findByEmail(dto.getEmail())
                .map(participant -> validateExistingParticipant(participant, dto))
                .orElseGet(() -> createNewParticipant(dto));
    }

    private Participant validateExistingParticipant(Participant participant, EventRegistrationDTO dto) {
        if (!participant.getFirstName().equals(dto.getFirstName()) ||
                !participant.getLastName().equals(dto.getLastName()) ||
                !participant.getPhone().equals(dto.getPhone())) {
            throw new IllegalStateException("Участник с таким email уже существует. Указанные данные не совпадают с существующими");
        }
        return participant;
    }

    private Participant createNewParticipant(EventRegistrationDTO dto) {
        Participant participant = new Participant();
        participant.setFirstName(dto.getFirstName());
        participant.setLastName(dto.getLastName());
        participant.setEmail(dto.getEmail());
        participant.setPhone(dto.getPhone());
        return participantRepository.save(participant);
    }

    private EventDTO convertToDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setRoomId(event.getRoom().getId());
        dto.setRoomName(event.getRoom().getName());
        return dto;
    }

    private ParticipantDTO convertToParticipantDTO(Participant participant) {
        return getParticipantDTO(participant);
    }

    public List<EventDTO> getEventsBetweenDates(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }

        return eventRepository.findEventsBetweenDates(start, end).stream()
                .filter(event -> !event.isDeleted())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<Event> getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime tomorrowEnd = todayStart.plusDays(2);

        return eventRepository.findUpcomingEvents(todayStart, tomorrowEnd);
    }
}
