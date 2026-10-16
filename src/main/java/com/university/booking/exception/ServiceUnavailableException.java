package com.university.booking.exception;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends BookingException {

    public ServiceUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}
