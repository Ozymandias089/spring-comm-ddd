package com.y11i.springcommddd.iam.infrastructure;

import com.y11i.springcommddd.iam.application.port.out.PasswordResetTokenPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RedisPasswordResetTokenAdapter implements PasswordResetTokenPort {

    private final StringRedisTemplate redisTemplate;

    private static String key(String token) {
        return "pwdReset:" + token;
    }

    /** {@inheritDoc} */
    @Override
    public String issueToken(UUID memberId, Duration ttl) {
        String token = UUID.randomUUID().toString();
        String k = key(token);
        redisTemplate.opsForValue().set(k, memberId.toString(), ttl);
        return token;
    }

    /** {@inheritDoc} */
    @Override
    public UUID consume(String token) {
        String k = key(token);
        String value = redisTemplate.opsForValue().get(k);
        if (value == null) throw new IllegalArgumentException("invalid or expired");
        redisTemplate.delete(k); // 1회용 소비
        return UUID.fromString(value);
    }
}
