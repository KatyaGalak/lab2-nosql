package com.university.booking.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BookingException {

    public ResourceNotFoundException(String id) {
        super(HttpStatus.NOT_FOUND, "Ресурс не найден: " + id);
    }
}
