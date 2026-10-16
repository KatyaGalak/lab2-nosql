package com.university.booking.model;

import com.university.booking.enums.BookingStatus;
import java.time.LocalDateTime;
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
public class HistoryEntry {

    private BookingStatus status;
    private LocalDateTime changedAt;
    private String author;
    private String reason;

    public static HistoryEntry of(BookingStatus status, String author, String reason) {
        return new HistoryEntry(status, LocalDateTime.now(), author, reason);
    }
}