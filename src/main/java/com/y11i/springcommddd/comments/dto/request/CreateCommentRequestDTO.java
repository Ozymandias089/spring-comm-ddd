package com.y11i.springcommddd.comments.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommentRequestDTO(
        @NotNull
        @Size(max = 10000, min = 1)
        String body
) {}
