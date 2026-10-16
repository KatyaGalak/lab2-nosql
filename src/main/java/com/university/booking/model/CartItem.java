package com.university.booking.model;

import com.university.booking.dto.BookingRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
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
public class CartItem {

    private String id;
    private String roomId;
    private String categoryId;
    private String eventName;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int participantCount;
    private String contactPhone;
    private LocalDateTime addedAt;

    public static CartItem from(BookingRequest request) {
        return CartItem.builder()
                .id(UUID.randomUUID().toString())
                .roomId(request.getRoomId())
                .categoryId(request.getCategoryId())
                .eventName(request.getEventName())
                .eventDate(request.getEventDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .participantCount(request.getParticipantCount())
                .contactPhone(request.getContactPhone())
                .addedAt(LocalDateTime.now())
                .build();
    }

    public BookingRequest toBookingRequest() {
        return BookingRequest.builder()
                .roomId(roomId)
                .categoryId(categoryId)
                .eventName(eventName)
                .eventDate(eventDate)
                .startTime(startTime)
                .endTime(endTime)
                .participantCount(participantCount)
                .contactPhone(contactPhone)
                .build();
    }
}
