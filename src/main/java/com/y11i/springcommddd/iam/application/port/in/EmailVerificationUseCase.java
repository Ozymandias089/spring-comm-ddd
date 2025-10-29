package com.y11i.springcommddd.iam.application.port.in;

import java.util.UUID;

public interface EmailVerificationUseCase {
    // 가입 인증: 토큰 발급(재발송)
    void requestForSignup(UUID memberId, String email);

    // 가입 인증 확인
    void confirmSignup(String token);

    // 이메일 변경: 토큰 발급
    void requestForChange(UUID memberId, String newEmail);

    // 이메일 변경 확인
    void confirmChange(String token);
}
