package com.university.booking.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RoomScheduleResponse(String roomId, LocalDate date, List<BusySlot> busy) {

    public record BusySlot(LocalTime startTime, LocalTime endTime) {
    }
}
