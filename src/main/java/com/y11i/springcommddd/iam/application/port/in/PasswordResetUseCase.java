package com.y11i.springcommddd.iam.application.port.in;

/**
 * 비밀번호 재설정(분실/복구) 유스케이스.
 *
 * <p>
 * 전형적인 "비밀번호를 잊어버렸습니다" 흐름을 처리한다:
 * <ol>
 *     <li>이메일을 제출하면, 해당 이메일 계정에 재설정 토큰을 전송한다.</li>
 *     <li>사용자는 토큰과 새 비밀번호를 제출하여 비밀번호를 재설정한다.</li>
 * </ol>
 * </p>
 *
 * <p>
 * 이 유스케이스는 '로그인된 사용자'가 아닐 수도 있는 요청을 처리한다.
 * 따라서 이 포트는 인증된 컨텍스트 밖에서도 호출 가능하다고 가정한다.
 * </p>
 */
public interface PasswordResetUseCase {

    /**
     * 비밀번호 재설정 토큰 발급을 요청한다.
     *
     * <p>구현체는:
     * <ul>
     *     <li>해당 이메일의 사용자가 존재하면 토큰을 생성하고 전송(이메일 등)</li>
     *     <li>존재하지 않더라도 "성공"처럼 응답해 공격자가 존재 여부를 유추하지 못하게 할 수도 있다</li>
     * </ul>
     * 이 정책은 구현체에 맡긴다.
     * </p>
     *
     * @param email 비밀번호를 초기화하고 싶은 계정의 이메일
     */
    void request(String email);

    /**
     * 발급된 토큰을 검증하고, 새 비밀번호로 갱신한다.
     *
     * @param token        비밀번호 재설정 토큰
     * @param newPassword  새 비밀번호(평문)
     */
    void confirm(String token, String newPassword);
}
