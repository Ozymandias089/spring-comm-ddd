package com.y11i.springcommddd.iam.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

public class EmailChangeRequestDTO {
    @Getter
    @NotBlank
    @Email
    private String email;
}
