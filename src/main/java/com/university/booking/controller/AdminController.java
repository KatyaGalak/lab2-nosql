package com.university.booking.controller;

import com.university.booking.config.MongoSchemaInitializer;
import com.university.booking.dto.ExplainComparison;
import com.university.booking.dto.MigrationResult;
import com.university.booking.enums.BookingStatus;
import com.university.booking.service.DemoDataService;
import com.university.booking.service.ExplainService;
import com.university.booking.web.AdminOnly;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
public class AdminController {

    private static final int MAX_DEMO_COUNT = 200_000;

    private final MongoSchemaInitializer mongoSchemaInitializer;
    private final DemoDataService demoDataService;
    private final ExplainService explainService;

    @AdminOnly
    @PostMapping("/migrations")
    public ResponseEntity<List<MigrationResult>> migrate() {
        return ResponseEntity.ok(mongoSchemaInitializer.migrateAndValidate());
    }

    @AdminOnly
    @PostMapping("/demo-data")
    public ResponseEntity<Map<String, Object>> generateDemoData(
            @RequestParam(name = "count", defaultValue = "50000") int count) {
        return ResponseEntity.ok(demoDataService.generate(Math.clamp(count, 1, MAX_DEMO_COUNT)));
    }

    @AdminOnly
    @GetMapping("/explain/author-status")
    public ResponseEntity<ExplainComparison> explainAuthorStatus(
            @RequestParam(name = "personId") String personId,
            @RequestParam(name = "status", defaultValue = "UNDER_REVIEW") BookingStatus status) {
        return ResponseEntity.ok(explainService.explainAuthorStatus(personId, status));
    }
}