package com.university.booking.service;

import com.university.booking.dto.BookingRequest;
import com.university.booking.enums.BookingStatus;
import com.university.booking.enums.PersonRole;
import com.university.booking.exception.AccessDeniedException;
import com.university.booking.exception.ConflictException;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.exception.ValidationException;
import com.university.booking.model.Booking;
import com.university.booking.model.Person;
import com.university.booking.model.Room;
import com.university.booking.repository.BookingRepository;
import com.university.booking.repository.CategoryRepository;
import com.university.booking.repository.RoomRepository;

import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final CategoryRepository categoryRepository;
    private final AccessService accessService;
    private final PersonLookupService personLookupService;

    public void validateBookingTarget(String personId, BookingRequest request) {
        Person person = personLookupService.findById(personId)
                .orElseThrow(() -> new ResourceNotFoundException(personId));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException(request.getRoomId()));

        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw new ResourceNotFoundException(request.getCategoryId());
        }

        if (room.isTeacherOnly() && person.getRole() == PersonRole.STUDENT) {
            throw new AccessDeniedException("Студенты не могут бронировать преподавательские коворкинги");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new ValidationException("Время окончания должно быть позже времени начала");
        }

        if (request.getParticipantCount() > room.getCapacity()) {
            throw new ValidationException("Помещение %s вмещает не больше %d человек"
                    .formatted(room.getId(), room.getCapacity()));
        }

        requireRoomFree(room.getId(), request.getEventDate(), request.getStartTime(), request.getEndTime());
    }

    public Booking createBooking(String personId, BookingRequest request) {
        return createBooking(personId, request, BookingStatus.DRAFT);
    }

    @Transactional
    public Booking createBooking(String personId, BookingRequest request, BookingStatus initialStatus) {
        validateBookingTarget(personId, request);

        Booking booking = Booking.builder()
                .id(UUID.randomUUID().toString())
                .personId(personId)
                .roomId(request.getRoomId())
                .categoryId(request.getCategoryId())
                .eventName(request.getEventName())
                .eventDate(request.getEventDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .participantCount(request.getParticipantCount())
                .contactPhone(request.getContactPhone())
                .status(initialStatus)
                .createdAt(LocalDateTime.now())
                .build();

        return bookingRepository.save(booking);
    }

    public Booking getBooking(String id, String currentPersonId) {
        Booking booking = findBooking(id);
        if (!isOwner(booking, currentPersonId) && !accessService.isAdmin(currentPersonId)) {
            throw new AccessDeniedException("Можно просматривать только свои заявки");
        }
        return booking;
    }

    public List<Booking> getBookings(String currentPersonId) {
        if (accessService.isAdmin(currentPersonId)) {
            return bookingRepository.findAllByOrderByCreatedAtAsc();
        }
        return bookingRepository.findByPersonIdOrderByCreatedAtAsc(currentPersonId);
    }

    public Booking submitBooking(String id, String currentPersonId) {
        Booking booking = findOwnBooking(id, currentPersonId, "Отправлять на рассмотрение можно только свои заявки");
        requireStatus(booking, BookingStatus.DRAFT);
        booking.setStatus(BookingStatus.UNDER_REVIEW);
        return bookingRepository.save(booking);
    }

    public Booking approveBooking(String id, String adminId) {
        return process(id, adminId, BookingStatus.APPROVED);
    }

    public Booking rejectBooking(String id, String adminId) {
        return process(id, adminId, BookingStatus.REJECTED);
    }

    public void deleteBooking(String id, String currentPersonId) {
        Booking booking = findOwnBooking(id, currentPersonId, "Удалять можно только свои заявки");
        bookingRepository.delete(booking);
    }

    private Booking process(String id, String adminId, BookingStatus result) {
        Booking booking = findBooking(id);
        requireStatus(booking, BookingStatus.UNDER_REVIEW);
        if (result == BookingStatus.APPROVED) {
            requireRoomFree(booking.getRoomId(), booking.getEventDate(), booking.getStartTime(), booking.getEndTime());
        }
        booking.setStatus(result);
        booking.setProcessedBy(adminId);
        booking.setProcessedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    private void requireRoomFree(String roomId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        List<Booking> conflicts = bookingRepository.findOverlapping(roomId, date, startTime, endTime,
                BookingStatus.APPROVED);
        if (!conflicts.isEmpty()) {
            Booking conflict = conflicts.get(0);
            throw new ConflictException("Помещение %s уже занято %s с %s до %s"
                    .formatted(roomId, date, conflict.getStartTime(), conflict.getEndTime()));
        }
    }

    private Booking findBooking(String id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
    }

    private Booking findOwnBooking(String id, String currentPersonId, String deniedMessage) {
        Booking booking = findBooking(id);
        if (!isOwner(booking, currentPersonId)) {
            throw new AccessDeniedException(deniedMessage);
        }
        return booking;
    }

    private boolean isOwner(Booking booking, String personId) {
        return booking.getPersonId().equals(personId);
    }

    private void requireStatus(Booking booking, BookingStatus expected) {
        if (booking.getStatus() != expected) {
            throw new ConflictException("Действие доступно только для заявки в статусе %s, текущий статус: %s"
                    .formatted(expected, booking.getStatus()));
        }
    }
}
