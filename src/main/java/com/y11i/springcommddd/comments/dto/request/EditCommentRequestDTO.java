package com.y11i.springcommddd.comments.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditCommentRequestDTO(
        @NotBlank
        @Size(max = 20000, min = 1)
        String body
) {}
