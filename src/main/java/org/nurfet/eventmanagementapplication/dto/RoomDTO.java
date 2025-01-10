package org.nurfet.eventmanagementapplication.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomDTO {

    private Long id;

    @NotBlank(message = "Необходимо указать название комнаты")
    private String name;

    @Min(value = 1, message = "Вместимость должна быть не менее 1")
    private int capacity;
}
