package com.y11i.springcommddd.iam.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public class PasswordResetRequestDTO {
    @Getter
    @Email
    @NotNull
    private String email;
}
