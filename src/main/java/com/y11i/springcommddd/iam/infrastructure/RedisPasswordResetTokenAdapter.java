package com.y11i.springcommddd.iam.infrastructure;

import com.y11i.springcommddd.iam.application.port.out.PasswordResetTokenPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@Repository
public class RedisPasswordResetTokenAdapter implements PasswordResetTokenPort {

    private final StringRedisTemplate redisTemplate;
    private final Supplier<String> tokenSupplier;

    /**
     * 운영용 기본 생성자.
     * Spring이 이걸 사용해서 빈을 만든다.
     */
    @Autowired
    public RedisPasswordResetTokenAdapter(StringRedisTemplate redisTemplate) {
        this(redisTemplate, () -> UUID.randomUUID().toString());
    }

    /**
     * 테스트/커스텀 전략용 생성자.
     */
    public RedisPasswordResetTokenAdapter(StringRedisTemplate redisTemplate, Supplier<String> tokenSupplier) {
        this.redisTemplate = redisTemplate;
        this.tokenSupplier = tokenSupplier;
    }

    private static String key(String token) {
        return "pwdReset:" + token;
    }

    /** {@inheritDoc} */
    @Override
    public String issueToken(UUID memberId, Duration ttl) {
        String token = tokenSupplier.get();
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
