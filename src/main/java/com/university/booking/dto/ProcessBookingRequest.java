package com.university.booking.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessBookingRequest {

    @Size(max = 500)
    private String reason;
}