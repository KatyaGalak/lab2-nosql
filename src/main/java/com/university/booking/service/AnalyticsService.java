package com.university.booking.service;

import com.university.booking.dto.OrganizationBookingCount;
import com.university.booking.dto.OrganizationStatsReport;
import com.university.booking.enums.OrganizationType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private static final String PERSONAL_NAME = "Лично (без организации)";

    private final MongoTemplate mongoTemplate;

    @Cacheable(cacheNames = AnalyticsCache.NAME, key = "'' + #p0 + ':' + #p1")
    public OrganizationStatsReport bookingsByOrganization(LocalDate from, LocalDate to) {
        log.atInfo()
                .addKeyValue("event", "cache_miss")
                .addKeyValue("cache", AnalyticsCache.NAME)
                .log("running aggregation pipeline in MongoDB");

        List<OrganizationBookingCount> items = new ArrayList<>();
        for (Document row : mongoTemplate.getCollection("organizations").aggregate(pipeline(from, to))) {
            items.add(toCount(row));
        }
        return new OrganizationStatsReport(from, to, LocalDateTime.now(), items);
    }

    List<Document> pipeline(LocalDate from, LocalDate to) {
        Document dateFilter = dateFilter(from, to);

        List<Document> perOrganization = new ArrayList<>();
        if (dateFilter != null) {
            perOrganization.add(new Document("$match", new Document("eventDate", dateFilter)));
        }
        perOrganization.add(groupByStatus());

        Document personalMatch = new Document("organizationId", null);
        if (dateFilter != null) {
            personalMatch.append("eventDate", dateFilter);
        }
        List<Document> personal = List.of(
                new Document("$match", personalMatch),
                groupByStatus(),
                new Document("$group", new Document("_id", null)
                        .append("byStatus", new Document("$push", new Document("k", "$_id").append("v", "$count")))
                        .append("total", new Document("$sum", "$count"))),
                new Document("$project", new Document("name", new Document("$literal", PERSONAL_NAME))
                        .append("total", 1)
                        .append("byStatus", new Document("$arrayToObject", "$byStatus"))));

        return List.of(
                new Document("$lookup", new Document("from", "bookings")
                        .append("localField", "_id")
                        .append("foreignField", "organizationId")
                        .append("pipeline", perOrganization)
                        .append("as", "byStatus")),
                new Document("$project", new Document("name", 1)
                        .append("type", 1)
                        .append("counterTotal", "$stats.total")
                        .append("total", new Document("$sum", "$byStatus.count"))
                        .append("byStatus", new Document("$arrayToObject", new Document("$map",
                                new Document("input", "$byStatus")
                                        .append("as", "s")
                                        .append("in", new Document("k", "$$s._id").append("v", "$$s.count")))))),
                new Document("$unionWith", new Document("coll", "bookings").append("pipeline", personal)),
                new Document("$sort", new Document("total", -1).append("name", 1)));
    }

    private static Document groupByStatus() {
        return new Document("$group", new Document("_id", "$status").append("count", new Document("$sum", 1)));
    }

    private Document dateFilter(LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return null;
        }
        Document filter = new Document();
        if (from != null) {
            filter.append("$gte", mongoTemplate.getConverter().convertToMongoType(from));
        }
        if (to != null) {
            filter.append("$lte", mongoTemplate.getConverter().convertToMongoType(to));
        }
        return filter;
    }

    private static OrganizationBookingCount toCount(Document row) {
        OrganizationBookingCount count = new OrganizationBookingCount();
        count.setOrganizationId(row.get("_id") != null ? row.get("_id").toString() : null);
        count.setName(row.getString("name"));
        if (row.getString("type") != null) {
            count.setType(OrganizationType.valueOf(row.getString("type")));
        }
        count.setTotal(number(row.get("total")));
        Document byStatus = row.get("byStatus", Document.class);
        if (byStatus != null) {
            count.setDraft(number(byStatus.get("DRAFT")));
            count.setUnderReview(number(byStatus.get("UNDER_REVIEW")));
            count.setApproved(number(byStatus.get("APPROVED")));
            count.setRejected(number(byStatus.get("REJECTED")));
        }
        if (row.get("counterTotal") != null) {
            count.setCounterTotal(number(row.get("counterTotal")));
        }
        return count;
    }

    private static long number(Object value) {
        return value instanceof Number n ? n.longValue() : 0;
    }
}