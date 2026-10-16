package com.university.booking.dto;

import java.time.LocalDateTime;

public record MigrationResult(String name, long matched, long modified, LocalDateTime executedAt) { }