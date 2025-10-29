package com.y11i.springcommddd.iam.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class MemberDTO {
    @Getter
    private UUID memberId;
    @Getter
    private String email;
    @Getter
    private String displayName;
    @Getter
    private Set<String> roles;
    @Getter
    private String status;                // 혹은 MemberStatus로 해도 됨
    @Getter
    private boolean passwordResetRequired;
    @Getter
    private Instant createdAt;
    @Getter
    private Instant updatedAt;
    @Getter
    private long version;
    @Getter
    private String profileImageUrl;
    @Getter
    private String bannerImageUrl;
    @Getter
    private boolean emailVerified;

    @Builder
    public MemberDTO(
            UUID memberId,
            String email,
            String displayName,
            Set<String> roles,
            String status,
            boolean passwordResetRequired,
            Instant createdAt,
            Instant updatedAt,
            long version,
            String profileImageUrl,
            String bannerImageUrl,
            boolean emailVerified
    ) {
        this.memberId = memberId;
        this.email = email;
        this.displayName = displayName;
        this.roles = roles;
        this.status = status;
        this.passwordResetRequired = passwordResetRequired;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
        this.profileImageUrl = profileImageUrl;
        this.bannerImageUrl = bannerImageUrl;
        this.emailVerified = emailVerified;
    }
}
