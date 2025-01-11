package org.nurfet.eventmanagementapplication.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.nurfet.eventmanagementapplication.dto.RoomDTO;
import org.nurfet.eventmanagementapplication.exception.ResourceNotFoundException;
import org.nurfet.eventmanagementapplication.model.Event;
import org.nurfet.eventmanagementapplication.model.Room;
import org.nurfet.eventmanagementapplication.repository.EventRepository;
import org.nurfet.eventmanagementapplication.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    private final EventRepository eventRepository;

    @Transactional
    public RoomDTO createRoom(RoomDTO dto) {
        Room room = new Room();
        room.setName(dto.getName());
        room.setCapacity(dto.getCapacity());

        room = roomRepository.save(room);
        return convertToDTO(room);
    }

    public List<RoomDTO> getAvailableRooms(LocalDateTime startTime, LocalDateTime endTime) {
        List<RoomDTO> availableRooms = roomRepository.findAvailableRooms(startTime, endTime)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        if (availableRooms.isEmpty()) {
            throw new ResourceNotFoundException("Не найдено ни одного доступного помещения на указанный период времени");
        }

        return availableRooms;
    }

    public RoomDTO getRoom(Long id) {
        return roomRepository.findByIdAndDeletedFalse(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Помещение не найдено"));
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Помещение не найдено"));

        // Проверяем, есть ли активные события в этом помещении
        List<Event> activeEvents = eventRepository.findEventsBetweenDates(
                        LocalDateTime.now(),
                        LocalDateTime.now().plusYears(100)
                ).stream()
                .filter(event -> event.getRoom().getId().equals(id) && !event.isDeleted())
                .toList();

        if (!activeEvents.isEmpty()) {
            throw new IllegalStateException(
                    String.format("Невозможно удалить помещение %d с активными событиями", activeEvents.size())
            );
        }

        roomRepository.softDelete(id);
    }

    private RoomDTO convertToDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setCapacity(room.getCapacity());
        return dto;
    }

    @Transactional
    public RoomDTO updateRoom(Long id, RoomDTO dto) {
        Room room = roomRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Помещение не найдено"));

        // Проверяем, не уменьшается ли вместимость при наличии активных событий
        if (dto.getCapacity() < room.getCapacity()) {
            List<Event> activeEvents = eventRepository.findEventsBetweenDates(
                            LocalDateTime.now(),
                            LocalDateTime.now().plusYears(100)
                    ).stream()
                    .filter(event -> event.getRoom().getId().equals(id) && !event.isDeleted())
                    .toList();

            if (!activeEvents.isEmpty()) {
                throw new IllegalStateException("Невозможно уменьшить вместимость помещения при активных мероприятиях");
            }
        }

        room.setName(dto.getName());
        room.setCapacity(dto.getCapacity());

        room = roomRepository.save(room);
        return convertToDTO(room);
    }

    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .filter(room -> !room.isDeleted())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
