package com.y11i.springcommddd.iam.application.port.in;

import com.y11i.springcommddd.iam.dto.MemberDTO;

/**
 * 일반 사용자 회원가입 유스케이스.
 *
 * <p>
 * 이 유스케이스는 신규 회원을 생성하고,
 * 필요한 초기 상태(예: ACTIVE, 기본 ROLE_USER, 이메일 미인증 상태 등)를 설정한다.
 *
 * 비밀번호는 이 단계에서 해시/인코딩되어 저장되어야 하며,
 * 원문 비밀번호는 저장하면 안 된다.
 * </p>
 */
public interface RegisterMemberUseCase {
    /**
     * 회원가입 명령 모델.
     *
     * @param email        로그인 ID로 사용할 이메일
     * @param displayName  초기 표시명(닉네임)
     * @param rawPassword  평문 비밀번호 (이 유스케이스 안에서 인코딩되어야 함)
     */
    record Command(String email, String displayName, String rawPassword) {}

    /**
     * 신규 회원을 생성하고 저장한다.
     *
     * @param cmd 가입 요청 정보
     * @return 생성된 회원의 현재 상태를 담은 DTO
     *         (예: id, roles, status, createdAt 등)
     */
    MemberDTO register(Command cmd);
}

