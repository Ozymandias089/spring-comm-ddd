package com.y11i.springcommddd.iam.api;

import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.application.port.in.FindMemberUseCase;
import com.y11i.springcommddd.iam.application.port.in.ManageProfileUseCase;
import com.y11i.springcommddd.iam.application.port.in.RegisterMemberUseCase;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.MemberDTO;
import com.y11i.springcommddd.iam.dto.request.*;
import com.y11i.springcommddd.iam.dto.response.LoginResponseDTO;
import com.y11i.springcommddd.iam.dto.response.MeResponseDTO;
import com.y11i.springcommddd.iam.dto.response.RegisterResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class MemberController {
    private final RegisterMemberUseCase registerMemberUseCase;
    private final AuthenticationManager authenticationManager;
    private final FindMemberUseCase findMemberUseCase;
    private final ManageProfileUseCase manageProfileUseCase;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO requestDto) {
        RegisterMemberUseCase.Command command =
                new RegisterMemberUseCase.Command(requestDto.getEmail(), requestDto.getDisplayName(), requestDto.getPassword());

        MemberDTO memberDTO = registerMemberUseCase.register(command);
        RegisterResponseDTO body = MemberMapper.toRegisterResponseDTO(memberDTO);

        URI location = URI.create("/api/members/" + body.getMemberId());
        return ResponseEntity.created(location).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDto, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken token =  new UsernamePasswordAuthenticationToken(requestDto.getEmail(), requestDto.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        request.getSession(true);

        // principal 은 email (MemberAuthProvider 구현 상)
        String email = authentication.getName();
        MemberDTO memberDTO = findMemberUseCase.findByEmail(email).orElseThrow(); // 이 경우 거의 없지만 방어

        LoginResponseDTO body = MemberMapper.toLoginResponseDTO(memberDTO);
        // 로그인은 200 OK가 자연스럽다. (원하면 204 No Content도 가능)
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public MeResponseDTO me(@AuthenticatedMember MemberId memberId) {
        MemberDTO memberDTO = findMemberUseCase.findById(memberId.id()).orElseThrow();
        return MemberMapper.toMeResponseDTO(memberDTO);
    }

    @PatchMapping("/me/display-name")
    public MeResponseDTO rename(@AuthenticatedMember MemberId memberId, @Valid @RequestBody RenameRequestDTO requestDto) {
        MemberDTO memberDTO = manageProfileUseCase.rename(
                new ManageProfileUseCase.RenameCommand(memberId.id(), requestDto.getDisplayName())
        );
        return MemberMapper.toMeResponseDTO(memberDTO);
    }

    @PatchMapping("/me/email")
    public MeResponseDTO changeEmail(@AuthenticatedMember MemberId memberId, @Valid @RequestBody ChangeEmailRequestDTO requestDto) {
        MemberDTO memberDTO = manageProfileUseCase.changeEmail(
                new ManageProfileUseCase.ChangeEmailCommand(memberId.id(), requestDto.getEmail())
        );
        return MemberMapper.toMeResponseDTO(memberDTO);
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticatedMember MemberId memberId, @Valid @RequestBody ChangePasswordRequestDTO requestDto) {
        manageProfileUseCase.changePassword(
                new ManageProfileUseCase.ChangePasswordCommand(memberId.id(), requestDto.getNewPassword(), requestDto.getCurrentPassword())
        );
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/profile-image")
    public MeResponseDTO changeProfileImage(@AuthenticatedMember MemberId memberId, @Valid @RequestBody ChangeProfileImageRequestDTO requestDto) {
        MemberDTO memberDTO = manageProfileUseCase.changeProfileImage(
                new ManageProfileUseCase.ChangeProfileImageCommand(memberId.id(), requestDto.getProfileImageUrl())
        );
        return MemberMapper.toMeResponseDTO(memberDTO);
    }

    @PatchMapping("/me/banner-image")
    public MeResponseDTO changeBannerImage(@AuthenticatedMember MemberId memberId, @Valid @RequestBody ChangeBannerImageRequestDTO requestDto) {
        MemberDTO memberDTO = manageProfileUseCase.changeBannerImage(
                new ManageProfileUseCase.ChangeBannerImageCommand(memberId.id(), requestDto.getBannerImageUrl())
        );
        return MemberMapper.toMeResponseDTO(memberDTO);
    }
}
