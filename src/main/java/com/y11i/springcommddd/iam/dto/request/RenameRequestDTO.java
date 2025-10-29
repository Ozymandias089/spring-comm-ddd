package com.y11i.springcommddd.iam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class RenameRequestDTO {
    @Getter
    @NotBlank
    @Size(min = 2, max = 50)
    private String displayName;
}
