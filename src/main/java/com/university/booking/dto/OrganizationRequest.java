package com.university.booking.dto;

import com.university.booking.enums.OrganizationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganizationRequest {

    @Pattern(regexp = "^[a-z0-9-]{2,40}$")
    private String id;

    @NotBlank
    private String name;

    @NotNull
    private OrganizationType type;

    private String contact;

    private Boolean active;
}