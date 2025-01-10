package org.nurfet.eventmanagementapplication.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipantDTO {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;
}
