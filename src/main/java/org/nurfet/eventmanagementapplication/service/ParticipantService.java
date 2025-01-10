package org.nurfet.eventmanagementapplication.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nurfet.eventmanagementapplication.dto.EventRegistrationDTO;
import org.nurfet.eventmanagementapplication.dto.ParticipantDTO;
import org.nurfet.eventmanagementapplication.exception.ResourceNotFoundException;
import org.nurfet.eventmanagementapplication.model.Event;
import org.nurfet.eventmanagementapplication.model.Participant;
import org.nurfet.eventmanagementapplication.repository.EventRepository;
import org.nurfet.eventmanagementapplication.repository.ParticipantRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;

    private final EventRepository eventRepository;

    public ParticipantDTO getParticipant(Long id) {
        return participantRepository.findByIdAndDeletedFalse(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Участник не найден"));
    }

    @Transactional
    public void deleteParticipant(Long id) {
        Participant participant = participantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Участник не найден"));

        // Проверяем, есть ли активные события у участника
        List<Event> activeEvents = eventRepository.findByParticipantId(id)
                .stream()
                .filter(event -> !event.isDeleted() &&
                        event.getEndTime().isAfter(LocalDateTime.now()))
                .toList();

        log.info("Найдено {} активных событий для участника {}", activeEvents.size(), id);

        if (!activeEvents.isEmpty()) {
            String errorMessage = String.format("Невозможно удалить участника %d с активными событиями", activeEvents.size());
            log.warn(errorMessage);
            throw new IllegalStateException(errorMessage);
        }

        participantRepository.softDelete(id);
    }

    @Transactional
    public ParticipantDTO updateParticipant(Long id, EventRegistrationDTO dto) {
        Participant participant = participantRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Участник не найден"));

        // Проверяем, не занят ли email другим участником
        if (!participant.getEmail().equals(dto.getEmail())) {
            Optional<Participant> existingParticipant = participantRepository.findByEmail(dto.getEmail());
            if (existingParticipant.isPresent() && !existingParticipant.get().getId().equals(id)) {
                throw new IllegalStateException("Электронная почта уже используется другим участником");
            }
        }

        participant.setFirstName(dto.getFirstName());
        participant.setLastName(dto.getLastName());
        participant.setEmail(dto.getEmail());
        participant.setPhone(dto.getPhone());

        participant = participantRepository.save(participant);
        return convertToDTO(participant);
    }

    private ParticipantDTO convertToDTO(Participant participant) {
        return getParticipantDTO(participant);
    }

    static ParticipantDTO getParticipantDTO(Participant participant) {
        ParticipantDTO dto = new ParticipantDTO();
        dto.setId(participant.getId());
        dto.setFirstName(participant.getFirstName());
        dto.setLastName(participant.getLastName());
        dto.setEmail(participant.getEmail());
        dto.setPhone(participant.getPhone());
        return dto;
    }
}
