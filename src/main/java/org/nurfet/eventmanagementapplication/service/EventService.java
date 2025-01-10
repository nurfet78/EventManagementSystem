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
import java.util.Optional;
import java.util.stream.Collectors;

import static org.nurfet.eventmanagementapplication.service.ParticipantService.getParticipantDTO;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;

    @Transactional
    public EventDTO createEvent(EventDTO dto) {
        validateEventTimes(dto);

        Room room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Помещение не найдено"));

        validateRoomAvailability(room.getId(), dto.getStartTime(), dto.getEndTime());

        Event event = new Event();
        event.setName(dto.getName());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());
        event.setRoom(room);

        event = eventRepository.save(event);
        return convertToDTO(event);
    }

    private void validateRoomAvailability(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        if (eventRepository.existsOverlappingEvent(roomId, startTime, endTime)) {
            throw new IllegalArgumentException("Помещение уже забронировано на это время");
        }
    }

    public List<EventDTO> getEventsBetweenDates(LocalDateTime start, LocalDateTime end) {
        return eventRepository.findEventsBetweenDates(start, end)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EventDTO getEvent(Long id) {
        return eventRepository.findByIdAndDeletedFalse(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Событие не найдено"));
    }

    private void validateEventTimes(EventDTO dto) {
        LocalDateTime now = LocalDateTime.now();

        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new IllegalArgumentException("Время окончания не может быть раньше времени начала");
        }

        // При создании события проверяем, что оно в будущем
        if (dto.getStartTime().isBefore(now.toLocalDate().atStartOfDay())) {
            throw new IllegalArgumentException("Дата начала не может быть в прошлом");
        }
    }

    private EventDTO convertToDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setStartTime(event.getStartTime());
        dto.setEndTime(event.getEndTime());
        dto.setRoomId(event.getRoom().getId());
        return dto;
    }

    public List<Event> getUpcomingEvents() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        return eventRepository.findEventsBetweenDates(start, end);
    }

    @Transactional
    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Событие не найдено"));

        // Мягкое удаление
        eventRepository.softDelete(id);
    }

    @Transactional
    public ParticipantDTO registerParticipant(Long eventId, EventRegistrationDTO registrationDTO) {
        Event event = eventRepository.findByIdAndDeletedFalse(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Событие не найдено"));

        // Проверяем, не прошло ли событие
        if (event.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Невозможно зарегистрироваться на прошедшее мероприятие");
        }

        // Проверяем, не используется ли email другим участником
        Optional<Participant> existingParticipant = participantRepository.findByEmail(registrationDTO.getEmail());

        if (existingParticipant.isPresent()) {
            throw new IllegalStateException("Email уже используется другим участником");
        }

        // Создаем нового участника
        Participant participant = new Participant();
        participant.setFirstName(registrationDTO.getFirstName());
        participant.setLastName(registrationDTO.getLastName());
        participant.setEmail(registrationDTO.getEmail());
        participant.setPhone(registrationDTO.getPhone());
        participant = participantRepository.save(participant);

        // Добавляем участника к событию
        event.getParticipants().add(participant);
        eventRepository.save(event);

        return convertToParticipantDTO(participant);
    }

    private ParticipantDTO convertToParticipantDTO(Participant participant) {
        return getParticipantDTO(participant);
    }

    @Transactional
    public EventDTO updateEvent(Long id, EventDTO dto) {
        Event event = eventRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Событие не найдено"));

        // При обновлении проверяем только логику времени (конец после начала)
        if (dto.getEndTime().isBefore(dto.getStartTime())) {
            throw new IllegalArgumentException("Время окончания не может быть раньше времени начала");
        }

        // Если меняется помещение или время, проверяем доступность
        if (!event.getRoom().getId().equals(dto.getRoomId()) ||
                !event.getStartTime().equals(dto.getStartTime()) ||
                !event.getEndTime().equals(dto.getEndTime())) {

            Room newRoom = roomRepository.findByIdAndDeletedFalse(dto.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Помещение не найдено"));

            validateRoomAvailability(newRoom.getId(), dto.getStartTime(), dto.getEndTime());
            event.setRoom(newRoom);
        }

        event.setName(dto.getName());
        event.setStartTime(dto.getStartTime());
        event.setEndTime(dto.getEndTime());

        event = eventRepository.save(event);
        return convertToDTO(event);
    }
}
