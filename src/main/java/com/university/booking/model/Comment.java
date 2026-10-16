package com.university.booking.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Field("id")
    private String id;
    private String authorId;
    private String text;
    private LocalDateTime createdAt;

    public static Comment of(String authorId, String text) {
        return Comment.builder()
                .id(UUID.randomUUID().toString())
                .authorId(authorId)
                .text(text)
                .createdAt(LocalDateTime.now())
                .build();
    }
}