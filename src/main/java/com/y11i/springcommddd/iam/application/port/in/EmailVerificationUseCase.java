package com.y11i.springcommddd.iam.application.port.in;

import java.util.UUID;

/**
 * 이메일 인증 관련 유스케이스.
 *
 * <p>
 * 이 유스케이스는 두 가지 흐름을 다룬다:
 * <ol>
 *     <li><b>회원가입 이메일 인증</b>:
 *         - 회원 가입 직후, 사용자의 이메일 주소로 인증 토큰을 발급/전달
 *         - 사용자는 토큰을 제출하여 이메일을 검증
 *     </li>
 *     <li><b>이메일 변경 인증</b>:
 *         - 로그인된 사용자가 신규 이메일로 변경을 요청할 때
 *         - 새 이메일 주소로 토큰을 전송
 *         - 토큰 검증 시 실제 이메일 변경 반영
 *     </li>
 * </ol>
 * </p>
 *
 * <p>
 * 토큰 생성/전달/검증의 저장 매커니즘은 out-port를 통해 인프라 계층(예: Redis, 메일 전송 등)에 위임된다.
 * </p>
 */
public interface EmailVerificationUseCase {
    /**
     * (회원가입 플로우) 주어진 회원에게 이메일 인증 토큰을 발급하고 전송한다.
     *
     * <p>이미 가입되어 있으나 아직 인증되지 않은 사용자가 "인증 메일 다시 보내기"를 눌렀을 때도 호출 가능.</p>
     *
     * @param memberId 갓 가입한(또는 아직 미인증 상태인) 회원의 고유 식별자(UUID)
     * @param email    인증 대상 이메일 주소
     */
    void requestForSignup(UUID memberId, String email);

    /**
     * (회원가입 플로우) 사용자가 받은 토큰을 제출해 가입용 이메일 인증을 완료한다.
     *
     * @param token 이메일 인증 토큰
     */
    void confirmSignup(String token);

    /**
     * (이메일 변경 플로우) 로그인된 사용자가 새 이메일로 바꾸려 할 때,
     * 그 새 이메일 주소에 인증 토큰을 전송한다.
     *
     * <p>이 호출은 "아직 변경 반영 전" 단계이다. 실제 변경은 {@link #confirmChange(String)}에서 일어난다.</p>
     *
     * @param memberId 변경을 요청한 회원의 고유 식별자(UUID)
     * @param newEmail 새로 설정하고자 하는 이메일
     */
    void requestForChange(UUID memberId, String newEmail);

    /**
     * (이메일 변경 플로우) 사용자가 이메일로 받은 토큰을 제출하여,
     * 해당 회원의 이메일 주소를 실제로 변경/확정한다.
     *
     * @param token 이메일 변경 확인 토큰
     */
    void confirmChange(String token);
}
