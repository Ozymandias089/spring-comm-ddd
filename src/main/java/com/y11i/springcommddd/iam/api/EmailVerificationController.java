package com.y11i.springcommddd.iam.api;

import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.application.port.in.EmailVerificationUseCase;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.request.EmailChangeRequestDTO;
import com.y11i.springcommddd.iam.dto.request.EmailVerificationConfirmRequestDTO;
import com.y11i.springcommddd.iam.dto.request.EmailVerificationSignupRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/email-verify")
public class EmailVerificationController {
    private final EmailVerificationUseCase emailVerificationUseCase;

    // 가입 인증 토큰 재발송
    @PostMapping("/signup/request/me")
    public ResponseEntity<Void> requestForSignup(@Valid @RequestBody EmailVerificationSignupRequestDTO requestDTO) {
        // 이메일만으로 재전송을 허용하려면, 이메일→멤버 조회 유스케이스를 써서 memberId를 찾아 호출하면 됨.
        // 하지만 여기선 간단하게 회원이 로그인 상태에서 내 주소로 재발송하는 API를 권장.
        return ResponseEntity.status(501).build();
    }

    @PostMapping("/signup/request")
    public ResponseEntity<Void> requestForSignupMe(@AuthenticatedMember MemberId memberId, @Valid @RequestBody EmailVerificationSignupRequestDTO requestDTO) {
        emailVerificationUseCase.requestForSignup(memberId.id(), requestDTO.getEmail());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/signup/confirm")
    public ResponseEntity<Void> confirmSignUp(@Valid @RequestBody EmailVerificationConfirmRequestDTO requestDTO) {
        emailVerificationUseCase.confirmSignup(requestDTO.getToken());
        return ResponseEntity.noContent().build();
    }

    // 이메일 변경 토큰 발급 (로그인 필요)
    @PostMapping("/change/request")
    public ResponseEntity<Void> requestForChange(@AuthenticatedMember MemberId memberId,
                                                 @Valid @RequestBody EmailChangeRequestDTO requestDTO) {
        emailVerificationUseCase.requestForChange(memberId.id(), requestDTO.getEmail());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/change/confirm")
    public ResponseEntity<Void> confirmChange(@Valid @RequestBody EmailVerificationConfirmRequestDTO requestDTO) {
        emailVerificationUseCase.confirmChange(requestDTO.getToken());
        return ResponseEntity.noContent().build();
    }
}
