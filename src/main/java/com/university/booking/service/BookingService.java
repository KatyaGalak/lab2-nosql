package com.university.booking.service;

import com.university.booking.dto.BookingRequest;
import com.university.booking.enums.BookingStatus;
import com.university.booking.enums.PersonRole;
import com.university.booking.exception.AccessDeniedException;
import com.university.booking.exception.ConflictException;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.exception.ValidationException;
import com.university.booking.model.Booking;
import com.university.booking.model.Comment;
import com.university.booking.model.HistoryEntry;
import com.university.booking.model.Person;
import com.university.booking.model.Room;
import com.university.booking.repository.BookingRepository;
import com.university.booking.repository.CategoryRepository;
import com.university.booking.repository.RoomRepository;

import lombok.AllArgsConstructor;

import org.bson.Document;

import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Service
@AllArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final CategoryRepository categoryRepository;
    private final AccessService accessService;
    private final PersonLookupService personLookupService;
    private final OrganizationService organizationService;
    private final AnalyticsCache analyticsCache;
    private final MongoTemplate mongoTemplate;

    public void validateBookingTarget(String personId, BookingRequest request) {
        Person person = personLookupService.findById(personId)
                .orElseThrow(() -> new ResourceNotFoundException(personId));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException(request.getRoomId()));

        if (!categoryRepository.existsById(request.getCategoryId())) {
            throw new ResourceNotFoundException(request.getCategoryId());
        }

        if (hasText(request.getOrganizationId())) {
            organizationService.requireActive(request.getOrganizationId());
        }

        if (room.isTeacherOnly() && person.getRole() == PersonRole.STUDENT) {
            throw new AccessDeniedException("Студенты не могут бронировать преподавательские коворкинги");
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new ValidationException("Время окончания должно быть позже времени начала");
        }

        if (!LocalDateTime.of(request.getEventDate(), request.getStartTime()).isAfter(LocalDateTime.now())) {
            throw new ValidationException("Нельзя бронировать на прошедшее время: начало %s %s уже наступило"
                    .formatted(request.getEventDate(), request.getStartTime()));
        }

        if (request.getParticipantCount() > room.getCapacity()) {
            throw new ValidationException("Помещение %s вмещает не больше %d человек"
                    .formatted(room.getId(), room.getCapacity()));
        }

        requireRoomFree(room.getId(), request.getEventDate(), request.getStartTime(), request.getEndTime());
    }

    @Transactional
    public Booking createBooking(String personId, BookingRequest request) {
        return createBooking(personId, request, BookingStatus.DRAFT);
    }

    @Transactional
    public Booking createBooking(String personId, BookingRequest request, BookingStatus initialStatus) {
        validateBookingTarget(personId, request);

        String reason = initialStatus == BookingStatus.DRAFT
                ? "Черновик создан"
                : "Заявка оформлена из корзины и отправлена на рассмотрение";

        Booking booking = Booking.builder()
                .id(UUID.randomUUID().toString())
                .personId(personId)
                .organizationId(hasText(request.getOrganizationId()) ? request.getOrganizationId() : null)
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
                .history(new ArrayList<>(List.of(HistoryEntry.of(initialStatus, personId, reason))))
                .build();

        Booking saved = bookingRepository.insert(booking);
        organizationService.moveStats(saved.getOrganizationId(), null, initialStatus);
        analyticsCache.invalidateAfterCommit();
        return saved;
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

    @Transactional
    public Booking submitBooking(String id, String currentPersonId) {
        Booking booking = findOwnBooking(id, currentPersonId, "Отправлять на рассмотрение можно только свои заявки");
        requireStatus(booking, BookingStatus.DRAFT);
        return changeStatus(booking, BookingStatus.UNDER_REVIEW, currentPersonId,
                "Отправлена на рассмотрение", null);
    }

    @Transactional
    public Booking approveBooking(String id, String adminId, String reason) {
        Booking booking = findBooking(id);
        requireStatus(booking, BookingStatus.UNDER_REVIEW);
        lockRoom(booking.getRoomId());
        requireRoomFree(booking.getRoomId(), booking.getEventDate(), booking.getStartTime(), booking.getEndTime());
        return changeStatus(booking, BookingStatus.APPROVED, adminId,
                hasText(reason) ? reason : "Одобрено администратором",
                hasText(reason) ? Comment.of(adminId, "Заявка одобрена: " + reason) : null);
    }

    @Transactional
    public Booking rejectBooking(String id, String adminId, String reason) {
        Booking booking = findBooking(id);
        requireStatus(booking, BookingStatus.UNDER_REVIEW);
        return changeStatus(booking, BookingStatus.REJECTED, adminId,
                hasText(reason) ? reason : "Отклонено администратором",
                hasText(reason) ? Comment.of(adminId, "Заявка отклонена: " + reason) : null);
    }

    @Transactional
    public void deleteBooking(String id, String currentPersonId) {
        Booking booking = findOwnBooking(id, currentPersonId, "Удалять можно только свои заявки");
        bookingRepository.delete(booking);
        organizationService.moveStats(booking.getOrganizationId(), booking.getStatus(), null);
        analyticsCache.invalidateAfterCommit();
    }

    public Booking addComment(String id, String currentPersonId, String text) {
        getBooking(id, currentPersonId);
        mongoTemplate.updateFirst(Query.query(where("id").is(id)),
                new Update().push("comments", Comment.of(currentPersonId, text)), Booking.class);
        return findBooking(id);
    }

    public Booking deleteComment(String id, String commentId, String currentPersonId) {
        Booking booking = getBooking(id, currentPersonId);
        Comment comment = booking.getComments().stream()
                .filter(c -> c.getId().equals(commentId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(commentId));
        if (!comment.getAuthorId().equals(currentPersonId) && !accessService.isAdmin(currentPersonId)) {
            throw new AccessDeniedException("Удалять можно только свои комментарии");
        }
        mongoTemplate.updateFirst(Query.query(where("id").is(id)),
                new Update().pull("comments", new Document("id", commentId)), Booking.class);
        return findBooking(id);
    }

    private Booking changeStatus(Booking booking, BookingStatus to, String author, String reason, Comment comment) {
        BookingStatus from = booking.getStatus();
        Update update = new Update()
                .set("status", to)
                .push("history", HistoryEntry.of(to, author, reason));
        if (to == BookingStatus.APPROVED || to == BookingStatus.REJECTED) {
            update.set("processedBy", author).set("processedAt", LocalDateTime.now());
        }
        if (comment != null) {
            update.push("comments", comment);
        }

        Booking updated = mongoTemplate.findAndModify(
                Query.query(where("id").is(booking.getId()).and("status").is(from)),
                update,
                FindAndModifyOptions.options().returnNew(true),
                Booking.class);
        if (updated == null) {
            throw new ConflictException("Заявка %s уже была изменена, обновите данные".formatted(booking.getId()));
        }

        organizationService.moveStats(updated.getOrganizationId(), from, to);
        analyticsCache.invalidateAfterCommit();
        return updated;
    }

    private void lockRoom(String roomId) {
        mongoTemplate.updateFirst(Query.query(where("id").is(roomId)),
                new Update().inc("approvalVersion", 1), Room.class);
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

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}