package com.y11i.springcommddd.iam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RenameRequestDTO(
        @NotBlank @Size(min = 2, max = 50) String displayName
) {}
