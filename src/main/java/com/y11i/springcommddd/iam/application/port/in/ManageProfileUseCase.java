package com.y11i.springcommddd.iam.application.port.in;

import com.y11i.springcommddd.iam.dto.MemberDTO;

import java.util.UUID;

public interface ManageProfileUseCase {
    MemberDTO rename(RenameCommand cmd);
    MemberDTO changeEmail(ChangeEmailCommand cmd);
    MemberDTO changePassword(ChangePasswordCommand cmd);

    MemberDTO changeProfileImage(ChangeProfileImageCommand command);
    MemberDTO changeBannerImage(ChangeBannerImageCommand command);

    record RenameCommand(UUID memberId, String displayName) {}
    record ChangeEmailCommand(UUID memberId, String email) {}
    record ChangePasswordCommand(UUID memberId, String rawPassword, String currentPassword) {}

    record ChangeProfileImageCommand(UUID memberId, String profileImageUrl) {}
    record ChangeBannerImageCommand(UUID memberId, String bannerImageUrl) {}
}
