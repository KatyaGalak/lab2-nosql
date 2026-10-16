package com.university.booking.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationStatsReport {

    private LocalDate from;
    private LocalDate to;

    private LocalDateTime generatedAt;
    private List<OrganizationBookingCount> items;
}