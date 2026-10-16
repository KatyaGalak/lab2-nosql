package com.university.booking.model;

import com.university.booking.enums.OrganizationType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "organizations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    @Id
    private String id;

    private String name;

    private OrganizationType type;

    private String contact;

    private boolean active;

    @Builder.Default
    private OrganizationStats stats = new OrganizationStats();

    private LocalDateTime createdAt;
}