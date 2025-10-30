package com.y11i.springcommddd.iam.application.port.in;

import java.util.UUID;

/**
 * 관리자(운영자) 권한 및 회원 상태를 관리하는 유스케이스.
 *
 * <p>
 * 주로 내부 운영(어드민 콘솔 등)에서 호출되며, 일반 사용자는 호출할 수 없다.
 * 이 유스케이스는 다음을 수행한다:
 * <ul>
 *     <li>특정 회원에게 관리자 권한을 부여 / 회수</li>
 *     <li>회원의 계정 상태(활성/정지/삭제 등) 변경</li>
 *     <li>신규 관리자 계정을 직접 생성</li>
 * </ul>
 * </p>
 *
 * <p>
 * 이 포트는 권한 체크/감사는 어댑터(API 계층 등)에서 수행된다고 가정하며,
 * 구현체는 올바른 호출자(이미 관리 권한이 있는 주체)만 접근한다고 믿는다.
 * </p>
 */
public interface AdminMemberUseCase {
    /**
     * 대상 회원에게 관리자 권한(예: ROLE_ADMIN 등)을 부여한다.
     *
     * @param cmd 대상 회원 ID를 포함한 명령
     */
    void grantAdmin(GrantAdminCommand cmd);

    /**
     * 대상 회원으로부터 관리자 권한을 회수한다.
     *
     * @param cmd 대상 회원 ID를 포함한 명령
     */
    void revokeAdmin(RevokeAdminCommand cmd);

    /**
     * 대상 회원의 계정 상태를 변경한다.
     *
     * <p>예: "ACTIVE", "SUSPENDED", "DELETED" 등</p>
     *
     * <p>구현체는 상태 전이 규칙(예: DELETED 이후 복구 불가 등)을 검증해야 한다.</p>
     *
     * @param cmd 대상 회원 ID와 신규 상태값
     */
    void setStatus(SetStatusCommand cmd);

    /**
     * 새로운 관리자 계정을 생성한다.
     *
     * <p>
     * 일반 회원 가입과 달리,
     * - 즉시 활성화된 관리자 권한을 부여할 수 있다
     * - 별도 이메일 인증 절차를 건너뛰는 등의 특수 플로우일 수 있다
     * </p>
     *
     * @param cmd 이메일 / 표시명 / 원문 비밀번호를 담은 명령
     * @return 생성된 관리자 계정의 회원 식별자 (MemberId 내부 UUID)
     */
    UUID createAdminAccount(CreateAdminCommand cmd); // 관리자 신규 계정 생성

    /**
     * 특정 회원에게 관리자 권한을 부여한다.
     *
     * @param targetMemberId 관리 권한을 부여할 대상 회원의 고유 식별자(UUID)
     */
    record GrantAdminCommand(UUID targetMemberId) {}

    /**
     * 특정 회원에게서 관리자 권한을 회수한다.
     *
     * @param targetMemberId 관리자 권한을 회수할 대상 회원의 고유 식별자(UUID)
     */
    record RevokeAdminCommand(UUID targetMemberId) {}

    /**
     * 특정 회원의 상태를 변경한다.
     *
     * <p>status 값은 애플리케이션 레벨에서 정의된 문자열 상태값과 일치해야 한다.
     * 예: "ACTIVE", "SUSPENDED", "DELETED"</p>
     *
     * @param targetMemberId 상태를 바꿀 회원의 고유 식별자(UUID)
     * @param status 새 상태 문자열
     */
    record SetStatusCommand(UUID targetMemberId, String status) {} // "ACTIVE"|"SUSPENDED"|"DELETED"

    /**
     * 신규 관리자 계정을 생성하기 위한 명령.
     *
     * @param email        관리 계정에 사용할 이메일(로그인 ID)
     * @param displayName  표시명(닉네임)
     * @param rawPassword  인코딩 전 비밀번호 원문
     */
    record CreateAdminCommand(String email, String displayName, String rawPassword) {}
}
