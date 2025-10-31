package com.y11i.springcommddd.iam.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class CreateAdminRequestDTO {
    @Getter @NotBlank
    @Email
    private String email;

    @Getter
    @NotBlank @Size(min = 2, max = 50)
    private String displayName;

    @Getter @NotBlank @Size(min = 8, max = 128)
    private String password;
}
