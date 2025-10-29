package com.y11i.springcommddd.iam.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class RegisterRequestDTO {
    @Getter
    @NotBlank
    private String email;
    @Getter
    @NotBlank
    private String displayName;
    @Getter
    @NotBlank
    private String password;
}
