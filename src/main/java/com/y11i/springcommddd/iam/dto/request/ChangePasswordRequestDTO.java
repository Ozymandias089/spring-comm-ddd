package com.y11i.springcommddd.iam.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

public class ChangePasswordRequestDTO {
    // 지금 ManageProfileUseCase는 현재 비밀번호를 요구하지 않으므로 newPassword만
    @Getter
    @NotBlank
    @Size(min = 8, max = 128)
    private String currentPassword;

    @Getter
    @NotBlank
    @Size(min = 8, max = 128)
    private String newPassword;
}
