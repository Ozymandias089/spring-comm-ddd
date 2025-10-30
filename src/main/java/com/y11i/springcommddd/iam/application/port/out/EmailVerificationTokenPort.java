package com.y11i.springcommddd.iam.application.port.out;

import java.time.Duration;
import java.util.UUID;

/**
 * 이메일 인증/변경을 위한 토큰 발급 및 검증(소비)용 출력 포트.
 *
 * <p>
 * 이메일 확인 플로우에는 두 가지 시나리오가 존재한다:
 * </p>
 *
 * <ol>
 *     <li><b>회원가입 이메일 인증</b><br/>
 *         - 새로 가입한 사용자의 이메일이 실제 본인 소유인지 확인하기 위해
 *           서버는 (memberId 기반으로) 토큰을 발급하고 전송한다.<br/>
 *         - 사용자가 그 토큰을 제출하면 유효성을 검증하고, 해당 회원의 이메일을
 *           "인증됨" 상태로 처리한다.
 *     </li>
 *
 *     <li><b>이메일 변경 인증</b><br/>
 *         - 기존 계정이 새 이메일로 변경하고자 할 때,
 *           (memberId + newEmail) 조합으로 토큰을 발급한다.<br/>
 *         - 사용자가 토큰을 제출하면, 해당 회원의 이메일 주소를 실제로 newEmail로 교체한다.
 *     </li>
 * </ol>
 *
 * <p>
 * 구현체는 일반적으로 단기 저장소(Redis 등)에
 * <code>token → payload</code> 형태로 매핑을 보관하며,
 * 만료 시간({@link Duration ttl}) 이후에는 토큰이 무효가 되어야 한다.
 * </p>
 *
 * <p>
 * "consume" 계열 메서드는 토큰을 1회용으로 간주하고,
 * 성공적으로 해석된 후에는 해당 토큰이 다시 사용되지 않도록 제거/무효화해야 한다.
 * (재사용 방지)
 * </p>
 */
public interface EmailVerificationTokenPort {
    /**
     * 회원가입 이메일 인증용 토큰을 발급한다.
     *
     * @param memberId 이메일 인증이 필요한 신규 회원의 식별자(UUID)
     * @param ttl      토큰 유효 시간 (예: 10분)
     * @return 발급된 토큰 문자열. 이 값은 이메일 본문에 포함되어 사용자에게 전달된다.
     */
    String issueForSignup(UUID memberId, Duration ttl);

    /**
     * 회원가입 이메일 인증 토큰을 소비(검증)한다.
     *
     * <p>
     * 이 메서드는 다음을 수행해야 한다:
     * </p>
     * <ul>
     *     <li>토큰이 유효/미만료인지 확인</li>
     *     <li>해당 토큰이 어떤 회원(UUID)에 대응하는지 복원</li>
     *     <li>토큰을 무효화하여 재사용 방지</li>
     * </ul>
     *
     * @param token 사용자가 제출한 토큰 문자열
     * @return 이 토큰이 가리키는 회원의 식별자(UUID)
     * @throws RuntimeException (구현체 선택) 토큰이 없거나 만료되었거나 이미 사용된 경우
     */
    UUID consumeForSignup(String token);

    /**
     * 이메일 변경(기존 계정 → 새 이메일) 확인용 토큰을 발급한다.
     *
     * @param memberId 변경을 요청한 회원의 식별자(UUID)
     * @param newEmail 새로 설정하고자 하는 이메일 주소
     * @param ttl      토큰 유효 시간
     * @return 발급된 토큰 문자열. 이 값은 새 이메일 주소로 전송된다.
     */
    String issueForChange(UUID memberId, String newEmail, Duration ttl);

    /**
     * 이메일 변경 확인 토큰을 소비(검증)한다.
     *
     * <p>
     * 이 메서드는 다음을 수행해야 한다:
     * </p>
     * <ul>
     *     <li>토큰이 유효/미만료인지 확인</li>
     *     <li>해당 토큰이 (어느 회원 UUID, 어떤 새 이메일)과 연결되는지 복원</li>
     *     <li>토큰을 무효화하여 재사용 방지</li>
     * </ul>
     *
     * @param token 사용자가 제출한 토큰 문자열
     * @return 해당 토큰에 대응하는 회원 ID와 새 이메일 주소
     * @throws RuntimeException (구현체 선택) 토큰이 없거나 만료되었거나 이미 사용된 경우
     */
    EmailChangePayload consumeForChange(String token);

    /**
     * 이메일 변경 토큰이 가리키는 정보 (memberId + newEmail).
     *
     * @param memberId  이메일을 변경하려는 기존 계정의 회원 식별자(UUID)
     * @param newEmail  최종적으로 반영할 새로운 이메일 주소
     */
    record EmailChangePayload(UUID memberId, String newEmail){}
}
