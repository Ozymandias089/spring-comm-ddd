package com.y11i.springcommddd.iam.dto.request.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public class RevokeAdminRequestDTO {
    @Getter @NotNull
    private String memberId;
}
