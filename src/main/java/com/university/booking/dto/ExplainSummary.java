package com.university.booking.dto;

public record ExplainSummary(String plan,
                             String indexName,
                             long nReturned,
                             long totalKeysExamined,
                             long totalDocsExamined,
                             long executionTimeMillis) { }