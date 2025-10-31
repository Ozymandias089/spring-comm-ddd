package com.y11i.springcommddd.iam.dto.request.admin;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.util.UUID;

public class SetStatusRequestDTO {
    @Getter
    @NotNull
    private UUID memberId;

    @Getter @NotNull
    @Pattern(regexp = "ACTIVE|SUSPENDED|DELETED")
    private String status;
}
