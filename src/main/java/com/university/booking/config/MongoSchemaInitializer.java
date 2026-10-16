package com.university.booking.config;

import com.university.booking.dto.MigrationResult;
import com.university.booking.service.MigrationService;
import com.university.booking.service.OrganizationService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class MongoSchemaInitializer implements ApplicationRunner {

    public static final String AUTHOR_STATUS_INDEX = "idx_person_status_created";

    private static final List<String> COLLECTIONS = List.of("persons", "rooms", "categories", "organizations",
            "bookings", "migrations");

    private final MongoTemplate mongoTemplate;
    private final MigrationService migrationService;
    private final OrganizationService organizationService;

    @Override
    public void run(ApplicationArguments args) {
        createCollections();
        migrateAndValidate();
        ensureIndexes();
    }

    public List<MigrationResult> migrateAndValidate() {
        List<MigrationResult> results = migrationService.migrate();
        if (results.stream().anyMatch(r -> r.modified() > 0)) {
            organizationService.recalculateStats();
        }
        applyValidator("bookings", "mongo/bookings.schema.json");
        applyValidator("organizations", "mongo/organizations.schema.json");
        return results;
    }

    private void createCollections() {
        for (String name : COLLECTIONS) {
            if (!mongoTemplate.collectionExists(name)) {
                mongoTemplate.createCollection(name);
            }
        }
    }

    private void applyValidator(String collection, String schemaResource) {
        Document command = new Document("collMod", collection)
                .append("validator", readJson(schemaResource))
                .append("validationLevel", "strict")
                .append("validationAction", "error");
        mongoTemplate.executeCommand(command);
        log.atInfo().addKeyValue("collection", collection).log("json schema validator applied");
    }

    private void ensureIndexes() {
        mongoTemplate.indexOps("bookings").createIndex(new Index()
                .on("personId", Sort.Direction.ASC)
                .on("status", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.DESC)
                .on("_id", Sort.Direction.DESC)
                .named(AUTHOR_STATUS_INDEX));

        mongoTemplate.indexOps("bookings").createIndex(new Index()
                .on("roomId", Sort.Direction.ASC)
                .on("eventDate", Sort.Direction.ASC)
                .on("status", Sort.Direction.ASC)
                .on("startTime", Sort.Direction.ASC)
                .named("idx_room_date_status"));

        mongoTemplate.indexOps("bookings").createIndex(new Index()
                .on("organizationId", Sort.Direction.ASC)
                .on("status", Sort.Direction.ASC)
                .named("idx_organization_status"));
    }

    private static Document readJson(String resource) {
        try (InputStream in = new ClassPathResource(resource).getInputStream()) {
            return Document.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read " + resource, e);
        }
    }
}