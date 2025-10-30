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

    public RegisterRequestDTO(String email, String displayName, String password) {
        this.email = email;
        this.displayName = displayName;
        this.password = password;
    }
}
