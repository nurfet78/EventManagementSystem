package org.nurfet.eventmanagementapplication.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.nurfet.eventmanagementapplication.dto.EventRegistrationDTO;
import org.nurfet.eventmanagementapplication.dto.ParticipantDTO;
import org.nurfet.eventmanagementapplication.service.ParticipantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    @GetMapping("/{id}")
    public ResponseEntity<ParticipantDTO> getParticipant(@PathVariable Long id) {
        return ResponseEntity.ok(participantService.getParticipant(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteParticipant(@PathVariable Long id) {
        participantService.deleteParticipant(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Участник с идентификатором %d был успешно удален", id));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateParticipant(
            @PathVariable Long id,
            @Valid @RequestBody EventRegistrationDTO registrationDTO) {
        ParticipantDTO updated = participantService.updateParticipant(id, registrationDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", String.format("Участник с идентификатором %d успешно обновлен", id));
        return ResponseEntity.ok(response);
    }
}
