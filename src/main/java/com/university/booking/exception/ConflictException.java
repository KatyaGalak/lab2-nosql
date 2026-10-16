package com.university.booking.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends BookingException {

    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
