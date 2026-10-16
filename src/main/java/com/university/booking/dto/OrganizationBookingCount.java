package com.university.booking.dto;

import com.university.booking.enums.OrganizationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrganizationBookingCount {

    private String organizationId;
    private String name;
    private OrganizationType type;
    private long total;
    private long draft;
    private long underReview;
    private long approved;
    private long rejected;

    private Long counterTotal;
}