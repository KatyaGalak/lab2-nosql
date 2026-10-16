package com.university.booking.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationStats {

    private long total;
    private long draft;
    private long underReview;
    private long approved;
    private long rejected;
}