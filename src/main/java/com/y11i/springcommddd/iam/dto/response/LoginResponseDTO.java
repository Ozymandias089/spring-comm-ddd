package com.y11i.springcommddd.iam.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

public class LoginResponseDTO {
    @Getter private String memberId;
    @Getter private String email;
    @Getter private String displayName;
    @Getter private Set<String> roles;
    @Getter private String status;
    @Getter private boolean passwordResetRequired;
    @Getter private String createdAt;
    @Getter private String updatedAt;
    @Getter private long version;

    @Builder
    public LoginResponseDTO(String memberId, String email, String displayName,
                            Set<String> roles, String status, boolean passwordResetRequired,
                            String createdAt, String updatedAt, long version) {
        this.memberId = memberId;
        this.email = email;
        this.displayName = displayName;
        this.roles = roles;
        this.status = status;
        this.passwordResetRequired = passwordResetRequired;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }
}
