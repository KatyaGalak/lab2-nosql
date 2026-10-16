package com.university.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    @NotBlank
    private String roomId;

    @NotBlank
    private String categoryId;

    @NotBlank
    private String eventName;

    @NotNull
    private LocalDate eventDate;

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    @Min(1)
    private int participantCount;

    @NotBlank
    @Pattern(regexp = "^\\+?\\d[\\d\\s()\\-]{8,18}\\d$",
            message = "номер телефона должен содержать 10–15 цифр, например +79991234567")
    private String contactPhone;

    private String organizationId;
}