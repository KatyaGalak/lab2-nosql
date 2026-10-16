package com.university.booking.model;

import com.university.booking.enums.PersonRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "persons")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    @Id
    private String isuId;

    private String lastName;

    private String firstName;

    private String middleName;

    private PersonRole role;
}