package com.y11i.springcommddd.iam.application.port.out;

import java.time.Duration;
import java.util.UUID;

public interface EmailVerificationTokenPort {
    // 가입 인증
    String issueForSignup(UUID memberId, Duration ttl);
    UUID consumeForSignup(String token);

    // 이메일 인증
    String issueForChange(UUID memberId, String newEmail, Duration ttl);
    EmailChangePayload consumeForChange(String token);

    record EmailChangePayload(UUID memberId, String newEmail){}
}
