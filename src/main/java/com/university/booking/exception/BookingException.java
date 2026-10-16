package com.university.booking.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BookingException extends RuntimeException {

    private final HttpStatus status;

    public BookingException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

}
