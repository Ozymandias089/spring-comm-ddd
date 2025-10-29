package com.y11i.springcommddd.iam.application.port.in;

import java.util.UUID;

public interface AdminMemberUseCase {
    void grantAdmin(GrantAdminCommand cmd);
    void revokeAdmin(RevokeAdminCommand cmd);
    void setStatus(SetStatusCommand cmd);
    UUID createAdminAccount(CreateAdminCommand cmd); // 관리자 신규 계정 생성

    record GrantAdminCommand(UUID targetMemberId) {}
    record RevokeAdminCommand(UUID targetMemberId) {}
    record SetStatusCommand(UUID targetMemberId, String status) {} // "ACTIVE"|"SUSPENDED"|"DELETED"
    record CreateAdminCommand(String email, String displayName, String rawPassword) {}
}
