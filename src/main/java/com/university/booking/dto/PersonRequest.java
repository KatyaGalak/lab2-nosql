package com.university.booking.dto;

import com.university.booking.enums.PersonRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonRequest {

    @NotBlank
    private String lastName;

    @NotBlank
    private String firstName;

    @NotBlank
    private String middleName;

    @NotNull
    private PersonRole role;
}
