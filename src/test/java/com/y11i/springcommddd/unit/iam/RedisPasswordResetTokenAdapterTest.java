package com.y11i.springcommddd.unit.iam;

import com.y11i.springcommddd.iam.infrastructure.RedisPasswordResetTokenAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisPasswordResetTokenAdapterTest {

    @Test
    @DisplayName("issueToken(): memberId를 TTL과 함께 pwdReset:{token} 키에 저장하고 토큰을 반환한다")
    void issueToken_storesMemberIdWithTtl_andReturnsToken() {
        // given
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);

        UUID memberId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Duration ttl = Duration.ofMinutes(5);

        String fixedToken = "reset-token-fixed-123";
        RedisPasswordResetTokenAdapter sut =
                new RedisPasswordResetTokenAdapter(redis, () -> fixedToken);

        // when
        String token = sut.issueToken(memberId, ttl);

        // then
        assertThat(token).isEqualTo(fixedToken);

        verify(valueOps).set(
                "pwdReset:" + fixedToken,
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                ttl
        );
    }

    @Test
    @DisplayName("consume(): 저장된 memberId를 UUID로 복원하고 키를 삭제한다. 없으면 IllegalArgumentException")
    void consume_returnsMemberId_andDeletesKey_elseThrows() {
        // given
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);

        String token = "tok-ppp";
        String key = "pwdReset:" + token;
        String storedMemberId = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

        RedisPasswordResetTokenAdapter sut =
                new RedisPasswordResetTokenAdapter(redis, () -> "unused-here");

        // 정상 케이스: 값 존재
        when(valueOps.get(key)).thenReturn(storedMemberId);

        UUID result = sut.consume(token);
        assertThat(result).isEqualTo(UUID.fromString(storedMemberId));
        verify(redis).delete(key);

        // 만료/없음: null이면 예외
        when(valueOps.get(key)).thenReturn(null);

        assertThatThrownBy(() -> sut.consume(token))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
