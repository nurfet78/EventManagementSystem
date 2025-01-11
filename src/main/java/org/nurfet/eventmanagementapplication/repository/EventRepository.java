package org.nurfet.eventmanagementapplication.repository;

import org.nurfet.eventmanagementapplication.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.deleted = false AND e.startTime BETWEEN :start AND :end")
    List<Event> findEventsBetweenDates(LocalDateTime start, LocalDateTime end);

    @Query("SELECT e FROM Event e JOIN e.participants p WHERE p.id = :participantId AND e.deleted = false")
    List<Event> findByParticipantId(Long participantId);

    @Query("SELECT e FROM Event e WHERE e.id = :id AND e.deleted = false")
    Optional<Event> findByIdAndDeletedFalse(@Param("id") Long id);

    @Query("SELECT e FROM Event e WHERE e.room.id = :roomId AND e.deleted = false")
    List<Event> findByRoomIdAndDeletedFalse(@Param("roomId") Long roomId);

    @Query("SELECT e FROM Event e WHERE e.deleted = false")
    List<Event> findAllByDeletedFalse();

    @Query("SELECT e FROM Event e " +
            "WHERE e.startTime BETWEEN :start AND :end " +
            "AND e.deleted = false " +
            "AND e.startTime > CURRENT_TIMESTAMP " +
            "ORDER BY e.startTime")
    List<Event> findUpcomingEvents(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
