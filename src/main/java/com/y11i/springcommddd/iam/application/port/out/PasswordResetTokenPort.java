package com.y11i.springcommddd.iam.application.port.out;

import java.time.Duration;
import java.util.UUID;

/**
 * 비밀번호 재설정(분실/복구) 토큰 발급 및 소비(검증)용 출력 포트.
 *
 * <p>
 * 전형적인 "비밀번호를 잊어버렸습니다" 흐름을 지원한다:
 * </p>
 *
 * <ol>
 *     <li>사용자가 이메일을 제출하면,
 *         해당 계정을 식별하는 토큰을 발급하고 이메일 등으로 전송한다.
 *     </li>
 *     <li>사용자는 토큰과 새 비밀번호를 서버에 제출한다.
 *         서버는 토큰을 검증/소비하고 실제 비밀번호를 갱신한다.
 *     </li>
 * </ol>
 *
 * <p>
 * 구현체는 일반적으로 단기 저장소(Redis 등)에
 * <code>token → memberId</code> 매핑을 TTL과 함께 저장한다.
 * </p>
 *
 * <p>
 * "consume"은 토큰의 단발성(1회성)을 보장해야 한다.
 * 한 번 성공적으로 소비된 토큰은 다시 사용할 수 없어야 한다.
 * </p>
 */
public interface PasswordResetTokenPort {
    /**
     * 주어진 회원에 대해 비밀번호 재설정 토큰을 발급한다.
     *
     * @param memberId 비밀번호를 초기화하려는 회원의 식별자(UUID)
     * @param ttl      토큰 유효 시간 (예: 10분, 30분 등)
     * @return 발급된 토큰 문자열. 호출자는 이 값을 메일 본문 등에 포함해 사용자에게 전달한다.
     */
    String issueToken(UUID memberId, Duration ttl);

    /**
     * 사용자가 제출한 토큰을 검증하고, 해당 토큰이 가리키는 회원 식별자를 반환한다.
     *
     * <p>
     * 구현체는 다음을 수행해야 한다:
     * </p>
     * <ul>
     *     <li>토큰이 존재하고 아직 만료되지 않았는지 확인</li>
     *     <li>해당 토큰이 연결된 {@code memberId}를 복원</li>
     *     <li>해당 토큰을 즉시 무효화하여 재사용을 방지</li>
     * </ul>
     *
     * @param token 사용자로부터 제출받은 비밀번호 재설정 토큰
     * @return 토큰이 가리키는 회원의 식별자(UUID)
     * @throws RuntimeException (구현체 기준) 토큰이 존재하지 않거나 만료되었거나 이미 소비된 경우
     */
    UUID consume(String token);
}
