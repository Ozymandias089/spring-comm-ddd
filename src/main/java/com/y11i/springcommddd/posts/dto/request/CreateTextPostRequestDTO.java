package com.y11i.springcommddd.posts.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTextPostRequestDTO(
        @NotBlank
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "communityId must be a UUID string")
        String communityId,

        @NotBlank @Size(max = 120)
        String title,

        @NotBlank @Size(max = 20000)
        String content
) {}
