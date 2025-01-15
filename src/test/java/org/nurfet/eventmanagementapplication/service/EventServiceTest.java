package org.nurfet.eventmanagementapplication.service;

import org.junit.jupiter.api.Test;
import org.nurfet.eventmanagementapplication.dto.EventDTO;
import org.nurfet.eventmanagementapplication.model.Event;
import org.nurfet.eventmanagementapplication.model.Room;
import org.nurfet.eventmanagementapplication.repository.EventRepository;
import org.nurfet.eventmanagementapplication.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@SpringBootTest
public class EventServiceTest {

    @MockitoBean
    private EventRepository eventRepository;

    @MockitoBean
    private RoomRepository roomRepository;

    @Autowired
    private EventService eventService;

    @Test
    void createEvent_WithOverlappingTime_ShouldThrowException() {
        //Запрос клиента
        EventDTO eventDTO = new EventDTO();
        eventDTO.setName("Test Event");
        eventDTO.setStartTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)); // 10:00
        eventDTO.setEndTime(LocalDateTime.now().plusDays(1).withHour(12).withMinute(0));   // 12:00
        eventDTO.setRoomId(1L);

        Room room = new Room();
        room.setId(1L);
        room.setName("Test Room");
        room.setCapacity(10);

        //Имитируем данные из базы данных
        Event existingEvent = new Event();
        existingEvent.setId(2L);
        existingEvent.setName("Existing Event");
        existingEvent.setStartTime(LocalDateTime.now().plusDays(1).withHour(11).withMinute(0)); // 11:00
        existingEvent.setEndTime(LocalDateTime.now().plusDays(1).withHour(13).withMinute(0));   // 13:00
        existingEvent.setRoom(room);
        existingEvent.setDeleted(false);

        System.out.println("\n=== Тест на пересечение времени ===");
        System.out.println("Новое мероприятие: " + eventDTO.getName());
        System.out.println("Время: " + eventDTO.getStartTime().toLocalTime() + " - " + eventDTO.getEndTime().toLocalTime());
        System.out.println("Существующее мероприятие: " + existingEvent.getName());
        System.out.println("Время: " + existingEvent.getStartTime().toLocalTime() + " - " + existingEvent.getEndTime().toLocalTime());

        when(roomRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(room));

        when(eventRepository.findByRoomIdAndDeletedFalse(1L))
                .thenReturn(List.of(existingEvent));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> eventService.createEvent(eventDTO));

        // Проверяем сообщение об ошибке
        assertEquals("Помещение уже забронировано на это время", exception.getMessage());

        // Проверяем, что save не вызывался
        verify(eventRepository, never()).save(any(Event.class));

        System.out.println("Тест успешно пройден!");
        System.out.println("Получено ожидаемое исключение: " + exception.getMessage());
        System.out.println("Сохранение в базу данных не производилось");
        System.out.println("=====================================\n");
    }
}
