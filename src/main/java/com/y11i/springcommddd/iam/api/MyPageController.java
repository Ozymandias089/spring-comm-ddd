package com.y11i.springcommddd.iam.api;

import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.api.support.MemberMapper;
import com.y11i.springcommddd.iam.application.port.in.FindMemberUseCase;
import com.y11i.springcommddd.iam.application.port.in.ManageProfileUseCase;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.MemberDTO;
import com.y11i.springcommddd.iam.dto.request.*;
import com.y11i.springcommddd.iam.dto.response.MyPageResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my-page")
@RequiredArgsConstructor
@Validated
public class MyPageController {
    private final FindMemberUseCase findMemberUseCase;
    private final ManageProfileUseCase manageProfileUseCase;

    @GetMapping
    public MyPageResponseDTO me(@AuthenticatedMember MemberId memberId) {
        MemberDTO memberDTO = findMemberUseCase.findById(memberId.id()).orElseThrow();
        return MemberMapper.toMyPageResponseDTO(memberDTO);
    }

    @PatchMapping("/display-name")
    public MyPageResponseDTO rename(@AuthenticatedMember MemberId memberId, @Valid @RequestBody RenameRequestDTO requestDto) {
        MemberDTO memberDTO = manageProfileUseCase.rename(
                new ManageProfileUseCase.RenameCommand(memberId.id(), requestDto.getDisplayName())
        );
        return MemberMapper.toMyPageResponseDTO(memberDTO);
    }

    @PatchMapping("/email")
    public MyPageResponseDTO changeEmail(@AuthenticatedMember MemberId memberId, @Valid @RequestBody ChangeEmailRequestDTO requestDto) {
        MemberDTO memberDTO = manageProfileUseCase.changeEmail(
                new ManageProfileUseCase.ChangeEmailCommand(memberId.id(), requestDto.getEmail())
        );
        return MemberMapper.toMyPageResponseDTO(memberDTO);
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticatedMember MemberId memberId, @Valid @RequestBody ChangePasswordRequestDTO requestDto) {
        manageProfileUseCase.changePassword(
                new ManageProfileUseCase.ChangePasswordCommand(memberId.id(), requestDto.getNewPassword(), requestDto.getCurrentPassword())
        );
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/profile-image")
    public MyPageResponseDTO changeProfileImage(@AuthenticatedMember MemberId memberId, @Valid @RequestBody ChangeProfileImageRequestDTO requestDto) {
        MemberDTO memberDTO = manageProfileUseCase.changeProfileImage(
                new ManageProfileUseCase.ChangeProfileImageCommand(memberId.id(), requestDto.getProfileImageUrl())
        );
        return MemberMapper.toMyPageResponseDTO(memberDTO);
    }

    @PatchMapping("/banner-image")
    public MyPageResponseDTO changeBannerImage(@AuthenticatedMember MemberId memberId, @Valid @RequestBody ChangeBannerImageRequestDTO requestDto) {
        MemberDTO memberDTO = manageProfileUseCase.changeBannerImage(
                new ManageProfileUseCase.ChangeBannerImageCommand(memberId.id(), requestDto.getBannerImageUrl())
        );
        return MemberMapper.toMyPageResponseDTO(memberDTO);
    }
}
