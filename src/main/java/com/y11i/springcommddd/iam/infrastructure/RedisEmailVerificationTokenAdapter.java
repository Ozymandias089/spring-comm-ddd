package com.y11i.springcommddd.iam.infrastructure;

import com.y11i.springcommddd.iam.application.port.out.EmailVerificationTokenPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

@Repository
public class RedisEmailVerificationTokenAdapter implements EmailVerificationTokenPort {
    private final StringRedisTemplate redis;
    private final Supplier<String> tokenSupplier;

    /**
     * 운영용 기본 생성자.
     * Spring이 이걸 사용해서 빈을 만든다.
     * tokenSupplier는 UUID.randomUUID() 기반.
     */
    @Autowired
    public RedisEmailVerificationTokenAdapter(StringRedisTemplate redis) {
        this(redis, () -> UUID.randomUUID().toString());
    }

    /**
     * 테스트/커스텀 토큰 전략용 생성자.
     * (스프링이 자동 주입할 필요는 없지만, public 으로 열어두면 단위 테스트에서 직접 new 할 수 있다.)
     */
    public RedisEmailVerificationTokenAdapter(StringRedisTemplate redis, Supplier<String> tokenSupplier) {
        this.redis = redis;
        this.tokenSupplier = tokenSupplier;
    }

    private static String signupKey(String token) { return "emailverify:signup:" + token; }

    /** {@inheritDoc} */
    @Override
    public String issueForSignup(UUID memberId, Duration ttl) {
        String token = tokenSupplier.get();
        String key = signupKey(token);
        redis.opsForValue().set(key, memberId.toString(), ttl);
        return token;
    }

    /** {@inheritDoc} */
    @Override
    public UUID consumeForSignup(String token) {
        String key = signupKey(token);
        String value = redis.opsForValue().get(key);
        if (value == null) throw new IllegalArgumentException("invalid or expired");
        redis.delete(key); // 1회용
        return UUID.fromString(value);
    }
}
