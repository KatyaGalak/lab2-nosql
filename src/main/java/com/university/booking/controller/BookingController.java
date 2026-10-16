package com.university.booking.controller;

import com.university.booking.dto.BookingRequest;
import com.university.booking.model.Booking;
import com.university.booking.service.BookingService;
import com.university.booking.web.AdminOnly;
import com.university.booking.web.PersonAuthInterceptor;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@AllArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<Booking> create(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId,
                                          @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(personId, request));
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAll(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId) {
        return ResponseEntity.ok(bookingService.getBookings(personId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getById(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId,
                                           @PathVariable("id") String id) {
        return ResponseEntity.ok(bookingService.getBooking(id, personId));
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<Booking> submit(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId,
                                          @PathVariable("id") String id) {
        return ResponseEntity.ok(bookingService.submitBooking(id, personId));
    }

    @AdminOnly
    @PutMapping("/{id}/approve")
    public ResponseEntity<Booking> approve(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String adminId,
                                           @PathVariable("id") String id) {
        return ResponseEntity.ok(bookingService.approveBooking(id, adminId));
    }

    @AdminOnly
    @PutMapping("/{id}/reject")
    public ResponseEntity<Booking> reject(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String adminId,
                                          @PathVariable("id") String id) {
        return ResponseEntity.ok(bookingService.rejectBooking(id, adminId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId,
                                       @PathVariable("id") String id) {
        bookingService.deleteBooking(id, personId);
        return ResponseEntity.noContent().build();
    }
}
