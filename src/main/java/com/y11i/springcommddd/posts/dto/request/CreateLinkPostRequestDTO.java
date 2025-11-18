package com.y11i.springcommddd.posts.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CreateLinkPostRequestDTO(
        @NotBlank
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "communityId must be a UUID string")
        String communityId,

        @NotBlank @Size(max = 120)
        String title,

        @NotBlank @Size(max = 1024)
        @URL(
                regexp = "^(http|https)://.*$",
                message = "link must be a valid http/https URL"
        )
        String link
) {}
