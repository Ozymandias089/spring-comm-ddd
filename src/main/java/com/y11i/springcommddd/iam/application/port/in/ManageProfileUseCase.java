package com.y11i.springcommddd.iam.application.port.in;

import com.y11i.springcommddd.iam.dto.MemberDTO;

import java.util.UUID;

public interface ManageProfileUseCase {
    MemberDTO rename(RenameCommand cmd);
    MemberDTO changeEmail(ChangeEmailCommand cmd);
    MemberDTO changePassword(ChangePasswordCommand cmd);

    record RenameCommand(UUID memberId, String displayName) {}
    record ChangeEmailCommand(UUID memberId, String email) {}
    record ChangePasswordCommand(UUID memberId, String rawPassword) {}
}
