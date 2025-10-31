package com.y11i.springcommddd.iam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class PasswordResetConfirmRequestDTO {
    @Getter
    @NotBlank
    private String token;

    @Getter @NotBlank @Size(min = 8, max = 128)
    private String newPassword;
}
