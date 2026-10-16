package com.university.booking.dto;

import com.university.booking.enums.BookingStatus;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class BookingSearchParams {

    private String personId;
    private BookingStatus status;
    private String roomId;
    private String categoryId;

    private String organizationId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate to;

    private String commentAuthor;
    private String commentText;

    private String historyAuthor;

    private String sort = "createdAt";
    private String direction = "desc";
    private int page = 0;
    private int size = 20;
}