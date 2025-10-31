package com.y11i.springcommddd.unit.iam;

import com.y11i.springcommddd.iam.application.port.out.EmailVerificationTokenPort;
import com.y11i.springcommddd.iam.infrastructure.RedisEmailVerificationTokenAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisEmailVerificationTokenAdapterTest {

    @Test
    @DisplayName("issueForSignup(): memberId를 TTL과 함께 signup 키로 저장하고 토큰을 반환한다")
    void issueForSignup_storesMemberIdWithTtl_andReturnsToken() {
        // given
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);

        UUID memberId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        Duration ttl = Duration.ofHours(24);

        String fixedToken = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
        RedisEmailVerificationTokenAdapter sut =
                new RedisEmailVerificationTokenAdapter(redis, () -> fixedToken);

        // when
        String token = sut.issueForSignup(memberId, ttl);

        // then
        assertThat(token).isEqualTo(fixedToken);

        verify(valueOps).set(
                "emailverify:signup:" + fixedToken,
                "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
                ttl
        );
    }

    @Test
    @DisplayName("consumeForSignup(): 저장된 memberId를 반환하고 키를 삭제한다. 없으면 IllegalArgumentException")
    void consumeForSignup_returnsMemberId_andDeletesKey_elseThrows() {
        // given
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);

        String token = "tok-123";
        String key = "emailverify:signup:" + token;
        String storedMemberId = "cccccccc-cccc-cccc-cccc-cccccccccccc";

        RedisEmailVerificationTokenAdapter sut =
                new RedisEmailVerificationTokenAdapter(redis, () -> "irrelevant-for-consume");

        // 정상 케이스: redis에 값이 있다
        when(valueOps.get(key)).thenReturn(storedMemberId);

        UUID result = sut.consumeForSignup(token);
        assertThat(result).isEqualTo(UUID.fromString(storedMemberId));
        verify(redis).delete(key);

        // 없는 케이스: null이면 예외
        when(valueOps.get(key)).thenReturn(null);

        assertThatThrownBy(() -> sut.consumeForSignup(token))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("issueForChange(): memberId|newEmail payload를 TTL과 함께 change 키에 저장하고 토큰을 반환한다")
    void issueForChange_storesCompositePayload_andReturnsToken() {
        // given
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);

        UUID memberId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
        String newEmail = "new@example.com";
        Duration ttl = Duration.ofHours(24);

        String fixedToken = "dddddddd-dddd-dddd-dddd-dddddddddddd";
        RedisEmailVerificationTokenAdapter sut =
                new RedisEmailVerificationTokenAdapter(redis, () -> fixedToken);

        // when
        String token = sut.issueForChange(memberId, newEmail, ttl);

        // then
        assertThat(token).isEqualTo(fixedToken);

        verify(valueOps).set(
                "emailverify:change:" + fixedToken,
                "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee|new@example.com",
                ttl
        );
    }

    @Test
    @DisplayName("consumeForChange(): payload를 파싱해 EmailChangePayload 반환하고 키를 삭제. 없거나 깨지면 IllegalArgumentException")
    void consumeForChange_parsesPayload_andDeletesKey_elseThrows() {
        // given
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valueOps);

        RedisEmailVerificationTokenAdapter sut =
                new RedisEmailVerificationTokenAdapter(redis, () -> "ignored");

        String token = "tok-xyz";
        String key = "emailverify:change:" + token;
        String payload = "ffffffff-ffff-ffff-ffff-ffffffffffff|care@example.com";

        // 정상 케이스
        when(valueOps.get(key)).thenReturn(payload);

        EmailVerificationTokenPort.EmailChangePayload result = sut.consumeForChange(token);
        assertThat(result.memberId()).isEqualTo(UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"));
        assertThat(result.newEmail()).isEqualTo("care@example.com");
        verify(redis).delete(key);

        // 값 없음 -> 예외
        when(valueOps.get(key)).thenReturn(null);
        assertThatThrownBy(() -> sut.consumeForChange(token))
                .isInstanceOf(IllegalArgumentException.class);

        // 깨진 payload -> 예외
        when(valueOps.get(key)).thenReturn("brokenpayloadwithoutseparator");
        assertThatThrownBy(() -> sut.consumeForChange(token))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
