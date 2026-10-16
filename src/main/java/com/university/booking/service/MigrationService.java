package com.university.booking.service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import com.university.booking.dto.MigrationResult;
import com.university.booking.model.Booking;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MigrationService {

    private static final String AUTHOR_FALLBACK_REASON = "Запись перенесена миграцией v2: причина не сохранялась";

    private final MongoTemplate mongoTemplate;

    public List<MigrationResult> migrate() {
        MongoCollection<Document> bookings = mongoTemplate.getCollection("bookings");
        return List.of(
                run("v1-bookings-history", bookings, historyMissing(), createHistory(), true),
                run("v2-history-author-reason", bookings, oldSchemaVersion(), addAuthorAndReason(), false));
    }

    private static Document historyMissing() {
        return new Document("history", new Document("$exists", false));
    }

    private static List<Document> createHistory() {
        return List.of(new Document("$set", new Document()
                .append("history", List.of(new Document()
                        .append("status", "$status")
                        .append("changedAt", new Document("$ifNull", List.of("$processedAt", "$createdAt")))))
                .append("schemaVersion", 1)));
    }

    private static Document oldSchemaVersion() {
        return new Document("$or", List.of(
                new Document("schemaVersion", new Document("$exists", false)),
                new Document("schemaVersion", new Document("$lt", Booking.SCHEMA_VERSION))));
    }

    private static List<Document> addAuthorAndReason() {
        Document processedByAdmin = new Document("$and", List.of(
                new Document("$in", List.of("$$h.status", List.of("APPROVED", "REJECTED"))),
                new Document("$ne", Arrays.asList(new Document("$ifNull", Arrays.asList("$processedBy", null)), null))));

        Document defaults = new Document()
                .append("author", new Document("$cond", List.of(processedByAdmin, "$processedBy", "$personId")))
                .append("reason", AUTHOR_FALLBACK_REASON);

        return List.of(new Document("$set", new Document()
                .append("history", new Document("$map", new Document()
                        .append("input", new Document("$ifNull", List.of("$history", List.of())))
                        .append("as", "h")
                        .append("in", new Document("$mergeObjects", List.of(defaults, "$$h")))))
                .append("comments", new Document("$ifNull", List.of("$comments", List.of())))
                .append("schemaVersion", Booking.SCHEMA_VERSION)));
    }

    private MigrationResult run(String name, MongoCollection<Document> collection,
                                Document filter, List<Document> pipeline, boolean intermediate) {
        UpdateResult result = collection.updateMany(filter, pipeline,
                new UpdateOptions().bypassDocumentValidation(intermediate));
        MigrationResult migration = new MigrationResult(name, result.getMatchedCount(), result.getModifiedCount(),
                LocalDateTime.now());

        mongoTemplate.getCollection("migrations").insertOne(new Document()
                .append("name", name)
                .append("matched", migration.matched())
                .append("modified", migration.modified())
                .append("executedAt", mongoTemplate.getConverter().convertToMongoType(migration.executedAt())));

        log.atInfo()
                .addKeyValue("event", "migration")
                .addKeyValue("name", name)
                .addKeyValue("matched", migration.matched())
                .addKeyValue("modified", migration.modified())
                .log("migration step executed");
        return migration;
    }
}