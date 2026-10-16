package com.university.booking.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.university.booking.enums.BookingStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "bookings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    public static final int SCHEMA_VERSION = 2;

    @Id
    private String id;

    private String personId;

    private String organizationId;

    private String roomId;

    private String categoryId;

    private String eventName;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int participantCount;
    private String contactPhone;

    private BookingStatus status;

    private String processedBy;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;

    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    private List<HistoryEntry> history = new ArrayList<>();

    @Builder.Default
    private int schemaVersion = SCHEMA_VERSION;

    @Transient
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> personNames;
}