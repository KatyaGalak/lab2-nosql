package com.university.booking.service;

import com.university.booking.dto.BookingSearchParams;
import com.university.booking.dto.PageResponse;
import com.university.booking.exception.AccessDeniedException;
import com.university.booking.exception.ValidationException;
import com.university.booking.model.Booking;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingSearchService {

    private static final int MAX_PAGE_SIZE = 100;

    private static final Map<String, String> SORT_FIELDS = Map.of(
            "createdAt", "createdAt",
            "eventDate", "eventDate",
            "startTime", "startTime",
            "status", "status",
            "participantCount", "participantCount");

    private final MongoTemplate mongoTemplate;
    private final AccessService accessService;

    public PageResponse<Booking> search(String currentPersonId, BookingSearchParams params) {
        int page = Math.max(params.getPage(), 0);
        int size = Math.clamp(params.getSize(), 1, MAX_PAGE_SIZE);

        Query query = new Query(buildCriteria(currentPersonId, params));
        long total = mongoTemplate.count(query, Booking.class);

        query.with(buildSort(params))
                .skip((long) page * size)
                .limit(size);
        List<Booking> items = mongoTemplate.find(query, Booking.class);

        return PageResponse.of(items, page, size, total);
    }

    private Criteria buildCriteria(String currentPersonId, BookingSearchParams params) {
        Criteria criteria = new Criteria();

        String author = params.getPersonId();
        if (!accessService.isAdmin(currentPersonId)) {
            if (author != null && !author.equals(currentPersonId)) {
                throw new AccessDeniedException("Искать можно только среди своих заявок");
            }
            author = currentPersonId;
        }

        if (hasText(author)) {
            criteria.and("personId").is(author);
        }
        if (params.getStatus() != null) {
            criteria.and("status").is(params.getStatus());
        }
        if (hasText(params.getRoomId())) {
            criteria.and("roomId").is(params.getRoomId());
        }
        if (hasText(params.getCategoryId())) {
            criteria.and("categoryId").is(params.getCategoryId());
        }
        if ("none".equals(params.getOrganizationId())) {
            criteria.and("organizationId").is(null);
        } else if (hasText(params.getOrganizationId())) {
            criteria.and("organizationId").is(params.getOrganizationId());
        }

        if (params.getFrom() != null || params.getTo() != null) {
            if (params.getFrom() != null && params.getTo() != null && params.getFrom().isAfter(params.getTo())) {
                throw new ValidationException("Дата from должна быть не позже даты to");
            }
            Criteria date = criteria.and("eventDate");
            if (params.getFrom() != null) {
                date.gte(params.getFrom());
            }
            if (params.getTo() != null) {
                date.lte(params.getTo());
            }
        }

        if (hasText(params.getCommentAuthor()) || hasText(params.getCommentText())) {
            Criteria comment = new Criteria();
            if (hasText(params.getCommentAuthor())) {
                comment.and("authorId").is(params.getCommentAuthor());
            }
            if (hasText(params.getCommentText())) {
                comment.and("text").regex(Pattern.quote(params.getCommentText()), "i");
            }
            criteria.and("comments").elemMatch(comment);
        }

        if (hasText(params.getHistoryAuthor())) {
            criteria.and("history.author").is(params.getHistoryAuthor());
        }

        return criteria;
    }

    private Sort buildSort(BookingSearchParams params) {
        String field = SORT_FIELDS.get(params.getSort());
        if (field == null) {
            throw new ValidationException("Сортировка возможна по полям: " + String.join(", ", SORT_FIELDS.keySet()));
        }
        Sort.Direction direction = "asc".equalsIgnoreCase(params.getDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return Sort.by(direction, field).and(Sort.by(direction, "id"));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}