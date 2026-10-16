package com.university.booking.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends BookingException {

    public ValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
