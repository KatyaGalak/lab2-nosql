package com.university.booking.enums;

import lombok.Getter;

@Getter
public enum BookingStatus {
    DRAFT("draft"),
    UNDER_REVIEW("underReview"),
    APPROVED("approved"),
    REJECTED("rejected");

    private final String statsField;

    BookingStatus(String statsField) {
        this.statsField = statsField;
    }
}