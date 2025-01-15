package org.nurfet.eventmanagementapplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EventRegistrationDTO {

    @NotBlank(message = "Необходимо указать имя")
    private String firstName;

    @NotBlank(message = "Необходимо указать фамилию")
    private String lastName;

    @Pattern(regexp = "^(\\w+\\.)*\\w+@(\\w+\\.)+[A-Za-z]+$", message = "Адрес электронной почты указан неверно")
    @NotBlank(message = "Поле email должно быть заполнено")
    private String email;

    @Pattern(regexp = "^\\+7\\s*\\(\\s*\\d{3}\\s*\\)\\s*\\d{3}\\s*-\\s*\\d{2}\\s*-\\s*\\d{2}$",
            message = "Номер телефона должен соответствовать формату: +7(XXX)XXX-XX-XX")
    private String phone;
}
