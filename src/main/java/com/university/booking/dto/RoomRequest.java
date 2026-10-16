package com.university.booking.dto;

import com.university.booking.enums.RoomType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomRequest {

    @NotBlank
    private String name;

    @NotNull
    private RoomType type;

    @Min(1)
    private int capacity;

    @NotBlank
    private String location;

    private boolean teacherOnly;
}
