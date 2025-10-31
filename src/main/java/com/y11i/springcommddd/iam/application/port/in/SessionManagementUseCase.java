package com.y11i.springcommddd.iam.application.port.in;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.SessionDTO;

import java.util.List;

/**
 * <h2>세션 관리 유스케이스</h2>
 *
 * <p>
 * 로그인된 사용자가 자신에게 연결된 모든 세션(브라우저, 기기 등)을
 * 조회하거나 개별 세션을 강제로 종료(무효화)하기 위한 애플리케이션 서비스 포트입니다.
 * </p>
 *
 * <h3>개요</h3>
 * <ul>
 *     <li>사용자는 여러 디바이스나 브라우저에서 동시에 로그인할 수 있습니다.</li>
 *     <li>이 유스케이스는 해당 사용자가 보유한 <b>활성 세션 목록</b>을 반환하거나,</li>
 *     <li>지정된 세션 ID를 강제로 종료시켜 <b>“다른 기기 로그아웃”</b>과 같은 기능을 제공합니다.</li>
 * </ul>
 *
 * <h3>식별 방식</h3>
 * <p>
 * 세션 소유자는 {@link MemberId}로 식별됩니다.
 * 스프링 시큐리티의 {@code Authentication.principal}은 {@code AuthenticatedMemberPrincipal}로 구현되며,
 * 이 안의 {@link MemberId} 값이 Spring Session 저장소에
 * <b>principalName(세션 인덱스 키)</b>로 기록됩니다.
 * 따라서 세션 검색은 {@code memberId.id().toString()}을 기준으로 수행됩니다.
 * </p>
 *
 * <h3>도메인적 의미</h3>
 * <p>
 * 본 유스케이스는 <i>회원이 스스로 자신의 세션을 관리</i>할 수 있게 하여
 * 계정 보안(세션 탈취 차단, 원격 로그아웃 등)을 강화하는 역할을 합니다.
 * 관리자는 이 유스케이스를 통해 특정 사용자의 세션을 관리하지 않습니다
 * (그런 기능은 별도의 어드민 유스케이스에서 다뤄질 수 있습니다).
 * </p>
 *
 * <h3>예외 및 제약</h3>
 * <ul>
 *     <li>요청자의 {@link MemberId}는 반드시 인증된 컨텍스트로부터 파생되어야 합니다.</li>
 *     <li>존재하지 않거나 이미 만료된 세션 ID를 지정하더라도 예외를 던지지 않고 무시할 수 있습니다.</li>
 *     <li>트랜잭션은 읽기 전용/삭제 단위로 분리됩니다.</li>
 * </ul>
 *
 * <h3>관련 인프라</h3>
 * <p>
 * 기본 구현체는 Spring Session의 {@link org.springframework.session.FindByIndexNameSessionRepository}를 사용하며,
 * Redis, JDBC 등 다양한 세션 저장소에 독립적으로 동작할 수 있습니다.
 * </p>
 *
 * @see com.y11i.springcommddd.iam.api.SessionsController
 * @see org.springframework.session.FindByIndexNameSessionRepository
 */
public interface SessionManagementUseCase {
    /**
     * 현재 로그인한 사용자의 활성 세션 목록을 조회합니다.
     *
     * <p>
     * 각 {@link SessionDTO}에는 세션 ID, 생성 시각, 최근 접근 시각, 세션 유효 시간 등이 포함됩니다.
     * </p>
     *
     * @param memberId 현재 로그인한 사용자의 고유 식별자
     * @return 사용자의 활성 세션 목록
     */
    List<SessionDTO> listMySessions(MemberId memberId);

    /**
     * 지정된 세션을 강제로 무효화(로그아웃)합니다.
     *
     * <p>
     * 대상 세션이 {@code memberId} 소유가 아닐 경우, 호출은 무시됩니다.
     * 존재하지 않거나 이미 만료된 세션 ID에 대해서도 예외를 던지지 않습니다.
     * </p>
     *
     * @param memberId  현재 로그인한 사용자의 고유 식별자
     * @param sessionId 무효화할 세션의 ID
     */
    void revokeSession(MemberId memberId, String sessionId);
}
