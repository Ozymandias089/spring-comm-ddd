package com.y11i.springcommddd.iam.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class LoginRequestDTO {
    @Getter @NotBlank @Email
    private String email;

    @Getter @NotBlank @Size(min = 8, max = 128)
    private String password;
}
