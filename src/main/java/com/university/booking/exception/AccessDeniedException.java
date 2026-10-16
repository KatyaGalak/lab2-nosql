package com.university.booking.exception;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends BookingException {

    public AccessDeniedException(String reason) {
        super(HttpStatus.FORBIDDEN, reason);
    }
}
