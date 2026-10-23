package com.university.booking.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.university.booking.config.MongoSchemaInitializer;
import com.university.booking.dto.ExplainComparison;
import com.university.booking.dto.ExplainSummary;
import com.university.booking.enums.BookingStatus;
import com.university.booking.model.Booking;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthorStatusIndexExplainTest {

    private static final int BOOKINGS = 20_000;
    private static final String AUTHOR = "200001";
    private static final BookingStatus STATUS = BookingStatus.UNDER_REVIEW;

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @Autowired
    private DemoDataService demoDataService;

    @Autowired
    private ExplainService explainService;

    @Autowired
    private MongoSchemaInitializer mongoSchemaInitializer;

    @Autowired
    private MongoTemplate mongoTemplate;

    private long totalBookings;

    @BeforeAll
    void fillCollection() {
        demoDataService.generate(BOOKINGS);
        totalBookings = mongoTemplate.count(new Query(), Booking.class);
    }

    @Test
    @DisplayName("с составным индексом запрос читает только найденные документы, без индекса — всю коллекцию")
    void compositeIndexAvoidsCollectionScan() {
        ExplainComparison explain = explainService.explainAuthorStatus(AUTHOR, STATUS);
        ExplainSummary without = explain.withoutIndex();
        ExplainSummary with = explain.withIndex();
        print("hint {$natural: 1}  (без индекса)", without);
        print("idx_person_status_created", with);

        assertThat(with.nReturned()).isPositive().isEqualTo(without.nReturned());

        assertThat(without.plan()).contains("COLLSCAN").contains("SORT");
        assertThat(without.totalKeysExamined()).isZero();
        assertThat(without.totalDocsExamined()).isEqualTo(totalBookings);

        assertThat(with.plan()).contains("IXSCAN").doesNotContain("SORT").doesNotContain("COLLSCAN");
        assertThat(with.indexName()).isEqualTo(MongoSchemaInitializer.AUTHOR_STATUS_INDEX);
        assertThat(with.totalKeysExamined()).isEqualTo(with.nReturned());
        assertThat(with.totalDocsExamined()).isEqualTo(with.nReturned());
        assertThat(with.totalDocsExamined() * 100).isLessThan(without.totalDocsExamined());
    }

    @Test
    @DisplayName("после удаления индекса планировщик возвращается к полному перебору коллекции")
    void withoutIndexPlannerFallsBackToCollectionScan() {
        mongoTemplate.indexOps(Booking.class).dropIndex(MongoSchemaInitializer.AUTHOR_STATUS_INDEX);
        try {
            ExplainSummary dropped = explainService.explainAuthorStatus(AUTHOR, STATUS).withIndex();
            print("индекс удалён", dropped);

            assertThat(dropped.indexName()).isNotEqualTo(MongoSchemaInitializer.AUTHOR_STATUS_INDEX);
            assertThat(dropped.totalDocsExamined()).isGreaterThan(dropped.nReturned() * 10);
        } finally {
            mongoSchemaInitializer.run(null);
        }

        assertThat(explainService.explainAuthorStatus(AUTHOR, STATUS).withIndex().indexName())
                .isEqualTo(MongoSchemaInitializer.AUTHOR_STATUS_INDEX);
    }

    private void print(String title, ExplainSummary s) {
        System.out.printf("%n%-36s план: %-40s ключей: %6d  документов: %6d  найдено: %4d  время: %d мс (всего заявок: %d)%n",
                title, s.plan(), s.totalKeysExamined(), s.totalDocsExamined(), s.nReturned(),
                s.executionTimeMillis(), totalBookings);
    }
}
