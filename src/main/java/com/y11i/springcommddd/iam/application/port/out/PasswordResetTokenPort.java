package com.y11i.springcommddd.iam.application.port.out;

import java.time.Duration;
import java.util.UUID;

public interface PasswordResetTokenPort {
    String issueToken(UUID memberId, Duration ttl);
    UUID consume(String token);
}
