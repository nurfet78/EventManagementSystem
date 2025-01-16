package org.nurfet.eventmanagementapplication.repository;

import org.junit.jupiter.api.Test;
import org.nurfet.eventmanagementapplication.model.Event;
import org.nurfet.eventmanagementapplication.model.Room;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


@DataJpaTest
@ActiveProfiles("test")
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByRoomIdAndDeletedFalse_ReturnOnlyActiveEvents() {
        Room room = new Room();
        room.setName("Конференц-зал А");
        room.setCapacity(60);
        entityManager.persist(room);

        Event activeEvent = new Event();
        activeEvent.setRoom(room);
        activeEvent.setDeleted(false);
        entityManager.persist(activeEvent);

        Event deleteEvent = new Event();
        deleteEvent.setRoom(room);
        deleteEvent.setDeleted(true);
        entityManager.persist(deleteEvent);

        List<Event> events = eventRepository.findByRoomIdAndDeletedFalse(room.getId());

        assertEquals(1, events.size());
        assertFalse(events.get(0).isDeleted());
    }
}
