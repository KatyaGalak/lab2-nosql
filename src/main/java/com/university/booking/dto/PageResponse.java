package com.university.booking.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public static <T> PageResponse<T> of(List<T> items, int page, int size, long total) {
        return new PageResponse<>(items, page, size, total, (int) Math.ceil((double) total / size));
    }
}