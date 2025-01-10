package org.nurfet.eventmanagementapplication.repository;

import org.nurfet.eventmanagementapplication.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.deleted = false AND e.room.id = :roomId " +
            "AND ((e.startTime <= :endTime) AND (e.endTime >= :startTime))")
    boolean existsOverlappingEvent(Long roomId, LocalDateTime startTime, LocalDateTime endTime);

    @Modifying
    @Query("UPDATE Event e SET e.deleted = true WHERE e.id = :id")
    void softDelete(Long id);

    @Query("SELECT e FROM Event e WHERE e.id = :id AND e.deleted = false")
    Optional<Event> findByIdAndDeletedFalse(Long id);
}
