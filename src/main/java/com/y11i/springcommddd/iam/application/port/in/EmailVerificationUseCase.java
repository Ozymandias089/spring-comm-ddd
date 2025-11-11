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
}
