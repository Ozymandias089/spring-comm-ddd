package com.y11i.springcommddd.iam.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class EmailVerificationConfirmRequestDTO {
    @Getter
    @NotBlank
    private String token;
}
