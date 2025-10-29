package com.y11i.springcommddd.iam.infrastructure;

import com.y11i.springcommddd.iam.application.port.out.EmailVerificationTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisEmailVerificationTokenAdapter implements EmailVerificationTokenPort {
    private final StringRedisTemplate redis;

    private static String signupKey(String token) { return "emailverify:signup:" + token; }
    private static String changeKey(String token) { return "emailverify:change:" + token; }

    @Override
    public String issueForSignup(UUID memberId, Duration ttl) {
        String token = UUID.randomUUID().toString();
        String key = signupKey(token);
        redis.opsForValue().set(key, memberId.toString(), ttl);
        return token;
    }

    @Override
    public UUID consumeForSignup(String token) {
        String key = signupKey(token);
        String value = redis.opsForValue().get(key);
        if (value == null) throw new IllegalArgumentException("invalid or expired");
        redis.delete(key); // 1회용
        return UUID.fromString(value);
    }

    @Override
    public String issueForChange(UUID memberId, String newEmail, Duration ttl) {
        String token = UUID.randomUUID().toString();
        String key = changeKey(token);
        String payload = memberId.toString() + "|" + newEmail;
        redis.opsForValue().set(key, payload, ttl);
        return token;
    }

    @Override
    public EmailChangePayload consumeForChange(String token) {
        String key = changeKey(token);
        String value = redis.opsForValue().get(key);
        if (value == null) throw new IllegalArgumentException("invalid or expired");
        redis.delete(key);

        int sep = value.indexOf('|');
        if (sep <= 0 || sep >= value.length() - 1) throw new IllegalArgumentException("invalid payload");
        UUID memberId = UUID.fromString(value.substring(0, sep));
        String newEmail = value.substring(sep + 1);
        return new EmailChangePayload(memberId, newEmail);
    }
}
