package com.university.booking.dto;

import java.util.Map;

public record ExplainComparison(Map<String, Object> filter,
                                Map<String, Object> sort,
                                ExplainSummary withoutIndex,
                                ExplainSummary withIndex) { }