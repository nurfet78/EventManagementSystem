package org.nurfet.eventmanagementapplication.repository;

import org.nurfet.eventmanagementapplication.model.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    @Query("SELECT p FROM Participant p WHERE p.deleted = false AND p.email = :email")
    Optional<Participant> findByEmail(String email);

    @Modifying
    @Query("UPDATE Participant p SET p.deleted = true WHERE p.id = :id")
    void softDelete(Long id);

    @Query("SELECT p FROM Participant p WHERE p.id = :id AND p.deleted = false")
    Optional<Participant> findByIdAndDeletedFalse(@Param("id") Long id);

}
