package com.y11i.springcommddd.iam.dto.request.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

public class GrantAdminRequestDTO {
    @Getter
    @NotNull
    private UUID memberId;
}
