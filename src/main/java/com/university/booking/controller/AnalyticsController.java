package com.university.booking.controller;

import com.university.booking.dto.OrganizationStatsReport;
import com.university.booking.service.AnalyticsService;
import com.university.booking.web.AdminOnly;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@AllArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @AdminOnly
    @GetMapping("/organizations")
    public ResponseEntity<OrganizationStatsReport> bookingsByOrganization(
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(analyticsService.bookingsByOrganization(from, to));
    }
}