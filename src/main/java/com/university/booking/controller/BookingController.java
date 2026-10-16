package com.university.booking.controller;

import com.university.booking.dto.BookingRequest;
import com.university.booking.dto.BookingSearchParams;
import com.university.booking.dto.CommentRequest;
import com.university.booking.dto.PageResponse;
import com.university.booking.dto.ProcessBookingRequest;
import com.university.booking.model.Booking;
import com.university.booking.service.BookingSearchService;
import com.university.booking.service.BookingService;
import com.university.booking.service.PersonNameService;
import com.university.booking.web.AdminOnly;
import com.university.booking.web.PersonAuthInterceptor;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    private final BookingSearchService bookingSearchService;
    private final PersonNameService names;

    @PostMapping
    public ResponseEntity<Booking> create(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId,
                                          @Valid @RequestBody BookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(names.withNames(bookingService.createBooking(personId, request)));
    }

    @GetMapping
    public ResponseEntity<List<Booking>> getAll(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId) {
        return ResponseEntity.ok(names.withNames(bookingService.getBookings(personId)));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<Booking>> search(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId,
                                                        @ModelAttribute BookingSearchParams params) {
        PageResponse<Booking> page = bookingSearchService.search(personId, params);
        names.withNames(page.getItems());
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Booking> getById(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId,
                                           @PathVariable("id") String id) {
        return ResponseEntity.ok(names.withNames(bookingService.getBooking(id, personId)));
    }

    @PutMapping("/{id}/submit")
    public ResponseEntity<Booking> submit(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId,
                                          @PathVariable("id") String id) {
        return ResponseEntity.ok(names.withNames(bookingService.submitBooking(id, personId)));
    }

    @AdminOnly
    @PutMapping("/{id}/approve")
    public ResponseEntity<Booking> approve(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String adminId,
                                           @PathVariable("id") String id,
                                           @Valid @RequestBody(required = false) ProcessBookingRequest request) {
        return ResponseEntity.ok(names.withNames(bookingService.approveBooking(id, adminId, reason(request))));
    }

    @AdminOnly
    @PutMapping("/{id}/reject")
    public ResponseEntity<Booking> reject(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String adminId,
                                          @PathVariable("id") String id,
                                          @Valid @RequestBody(required = false) ProcessBookingRequest request) {
        return ResponseEntity.ok(names.withNames(bookingService.rejectBooking(id, adminId, reason(request))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId,
                                       @PathVariable("id") String id) {
        bookingService.deleteBooking(id, personId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<Booking> addComment(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId,
                                              @PathVariable("id") String id,
                                              @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(names.withNames(bookingService.addComment(id, personId, request.getText())));
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    public ResponseEntity<Booking> deleteComment(@RequestAttribute(PersonAuthInterceptor.PERSON_ID_ATTRIBUTE) String personId,
                                                 @PathVariable("id") String id,
                                                 @PathVariable("commentId") String commentId) {
        return ResponseEntity.ok(names.withNames(bookingService.deleteComment(id, commentId, personId)));
    }

    private static String reason(ProcessBookingRequest request) {
        return request != null ? request.getReason() : null;
    }
}