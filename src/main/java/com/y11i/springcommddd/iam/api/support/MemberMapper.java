package com.y11i.springcommddd.iam.api.support;

import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.dto.MemberDTO;
import com.y11i.springcommddd.iam.dto.response.LoginResponseDTO;
import com.y11i.springcommddd.iam.dto.response.MyPageResponseDTO;
import com.y11i.springcommddd.iam.dto.response.RegisterResponseDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberMapper {
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    public static RegisterResponseDTO toRegisterResponseDTO(MemberDTO dto) {
        String created = dto.getCreatedAt() == null ? null : ISO.format(dto.getCreatedAt());
        String updated = dto.getUpdatedAt() == null ? null : ISO.format(dto.getUpdatedAt());

        return RegisterResponseDTO.builder()
                .memberId(dto.getMemberId().toString())
                .email(dto.getEmail())
                .displayName(dto.getDisplayName())
                .roles(dto.getRoles())
                .status(dto.getStatus())
                .passwordResetRequired(dto.isPasswordResetRequired())
                .createdAt(created)
                .updatedAt(updated)
                .version(dto.getVersion())
                .build();
    }

    public static LoginResponseDTO toLoginResponseDTO(MemberDTO dto) {
        return LoginResponseDTO.builder()
                .memberId(dto.getMemberId().toString())
                .email(dto.getEmail())
                .displayName(dto.getDisplayName())
                .roles(dto.getRoles())
                .status(dto.getStatus())
                .passwordResetRequired(dto.isPasswordResetRequired())
                .createdAt(dto.getCreatedAt() == null ? null : ISO.format(dto.getCreatedAt()))
                .updatedAt(dto.getUpdatedAt() == null ? null : ISO.format(dto.getUpdatedAt()))
                .version(dto.getVersion())
                .build();
    }

    public static MemberDTO toMemberDTO(Member member) {
        UUID memberId = member.memberId().id();
        String email = member.email().value();
        String displayName = member.displayName().value();
        Set<String> roles = member.roles().stream().map(Enum::name).collect(Collectors.toSet());
        String status = member.status().name();
        boolean passwordResetRequired = member.passwordResetRequired();

        return MemberDTO.builder()
                .memberId(memberId)
                .email(email)
                .displayName(displayName)
                .roles(roles)
                .status(status)
                .passwordResetRequired(passwordResetRequired)
                .createdAt(member.createdAt())
                .updatedAt(member.updatedAt())
                .version(member.version())
                .profileImageUrl(member.profileImage() == null ? null : member.profileImage().value())
                .bannerImageUrl(member.bannerImage() == null ? null : member.bannerImage().value())
                .emailVerified(member.emailVerified())
                .build();
    }

    public static MyPageResponseDTO toMyPageResponseDTO(MemberDTO dto) {
        String created = dto.getCreatedAt() == null ? null : ISO.format(dto.getCreatedAt());
        String updated = dto.getUpdatedAt() == null ? null : ISO.format(dto.getUpdatedAt());
        return MyPageResponseDTO.builder()
                .memberId(dto.getMemberId().toString())
                .email(dto.getEmail())
                .displayName(dto.getDisplayName())
                .roles(dto.getRoles())
                .status(dto.getStatus())
                .passwordResetRequired(dto.isPasswordResetRequired())
                .createdAt(created)
                .updatedAt(updated)
                .version(dto.getVersion())
                .profileImageUrl(dto.getProfileImageUrl())
                .bannerImageUrl(dto.getBannerImageUrl())
                .emailVerified(dto.isEmailVerified())
                .build();
    }
}
