package com.almedin.modules.shared.application.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PageResponse<T> of(List<T> content, PageRequest request, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / request.size());
        return new PageResponse<>(
                content,
                request.page(),
                request.size(),
                totalElements,
                totalPages,
                request.page() == 0,
                request.page() >= totalPages - 1
        );
    }
}