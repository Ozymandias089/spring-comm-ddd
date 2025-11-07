package com.y11i.springcommddd.iam.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class LoginRequestDTO {
    @Getter @NotBlank @Email
    private String email;

    @Getter @NotBlank @Size(min = 8, max = 128)
    private String password;

    @JsonCreator
    public LoginRequestDTO(@JsonProperty("email") String email, @JsonProperty("password") String password ) {
        this.email = email;
        this.password = password;
    }
}
