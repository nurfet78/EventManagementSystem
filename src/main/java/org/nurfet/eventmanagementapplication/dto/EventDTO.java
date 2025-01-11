package org.nurfet.eventmanagementapplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EventDTO {

    private Long id;

    @NotBlank(message = "Необходимо указать название мероприятия")
    private String name;

    @NotNull(message = "Необходимо указать время начала мероприятия")
    private LocalDateTime startTime;

    @NotNull(message = "Необходимо указать время окончания мероприятия")
    private LocalDateTime endTime;

    @NotNull(message = "Требуется идентификатор помещения")
    private Long roomId;

    private String roomName;
}
