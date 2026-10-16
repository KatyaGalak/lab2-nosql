package com.university.booking.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.university.booking.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rooms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    private String id;

    private String name;

    private RoomType type;

    private int capacity;

    private String location;

    private boolean teacherOnly;

    @JsonIgnore
    private long approvalVersion;
}