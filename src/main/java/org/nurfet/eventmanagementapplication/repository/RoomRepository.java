package org.nurfet.eventmanagementapplication.repository;

import org.nurfet.eventmanagementapplication.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r.deleted = false AND r.id NOT IN " +
            "(SELECT e.room.id FROM Event e WHERE e.deleted = false AND " +
            "((e.startTime <= :startTime AND e.endTime >= :startTime) OR " +
            "(e.startTime <= :endTime AND e.endTime >= :endTime) OR " +
            "(e.startTime >= :startTime AND e.endTime <= :endTime)))")
    List<Room> findAvailableRooms(LocalDateTime startTime, LocalDateTime endTime);

    @Modifying
    @Query("UPDATE Room r SET r.deleted = true WHERE r.id = :id")
    void softDelete(Long id);

    @Query("SELECT r FROM Room r WHERE r.id = :id AND r.deleted = false")
    Optional<Room> findByIdAndDeletedFalse(Long id);
}
