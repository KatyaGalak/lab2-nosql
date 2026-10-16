package com.university.booking.model;

import com.university.booking.enums.RoomType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    private String id;

    private String name;

    @Enumerated(EnumType.STRING)
    private RoomType type;

    private int capacity;

    private String location;

    @jakarta.persistence.Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean teacherOnly;
}
