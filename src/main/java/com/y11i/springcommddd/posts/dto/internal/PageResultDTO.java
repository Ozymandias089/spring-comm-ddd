package com.y11i.springcommddd.posts.dto.internal;

import java.util.List;

public record PageResultDTO<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {}

