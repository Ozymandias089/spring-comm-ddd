package com.y11i.springcommddd.iam.application.service;

import com.y11i.springcommddd.iam.application.port.in.SessionManagementUseCase;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.SessionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <h2>세션 관리 서비스 구현체</h2>
 *
 * <p>
 * {@link com.y11i.springcommddd.iam.application.port.in.SessionManagementUseCase}를 구현하며,
 * 로그인된 사용자의 세션 목록 조회 및 개별 세션 무효화 기능을 제공한다.
 * </p>
 *
 * <h3>개요</h3>
 * <ul>
 *     <li>Spring Session 저장소(예: Redis, JDBC)를 통해 세션 정보를 조회 및 삭제</li>
 *     <li>각 세션은 principalName 인덱스 키로 {@link MemberId#id()}(UUID 문자열)가 저장되어 있다고 가정</li>
 *     <li>사용자가 여러 기기/브라우저에서 로그인한 경우, 각 세션이 개별 엔트리로 관리된다</li>
 * </ul>
 *
 * <h3>주요 기능</h3>
 * <ul>
 *     <li>{@link #listMySessions(MemberId)} — 현재 사용자의 모든 활성 세션을 조회</li>
 *     <li>{@link #revokeSession(MemberId, String)} — 지정된 세션을 강제로 무효화(로그아웃)</li>
 * </ul>
 *
 * <h3>예외 및 처리 정책</h3>
 * <ul>
 *     <li>세션 조회 시에는 단순히 Redis 인덱스 기반으로 검색하며, 비어 있을 경우 빈 리스트 반환</li>
 *     <li>세션 무효화 시 본인 소유가 아닌 세션 ID이거나 이미 만료된 세션 ID는 조용히 무시</li>
 *     <li>트랜잭션은 읽기/쓰기 작업 별로 분리되어 있으며, delete 연산 시 새로운 트랜잭션이 시작됨</li>
 * </ul>
 *
 * <h3>사용 포트 및 의존성</h3>
 * <ul>
 *     <li>{@link org.springframework.session.FindByIndexNameSessionRepository}</li>
 *     <li>{@link com.y11i.springcommddd.iam.domain.MemberId}</li>
 *     <li>{@link com.y11i.springcommddd.iam.dto.SessionDTO}</li>
 * </ul>
 *
 * <h3>반환 데이터</h3>
 * <p>
 * 각 {@link SessionDTO}는 세션 ID, 생성 시각, 마지막 접근 시각, 세션 만료 시간 등을 포함한다.
 * 시간 필드는 ISO-8601 형식(UTC)으로 직렬화된다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class SessionManagementService implements SessionManagementUseCase {

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<SessionDTO> listMySessions(MemberId memberId) {
        var key = memberId.id().toString();

        return sessionRepository
                .findByIndexNameAndIndexValue(
                        FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, // ← 문자열 상수 사용
                        key
                )
                .values()
                .stream()
                .map(s -> SessionDTO.builder()
                        .sessionId(s.getId())
                        .creationTime(ISO.format(s.getCreationTime()))
                        .lastAccessedTime(ISO.format(s.getLastAccessedTime()))
                        .maxInactiveIntervalSeconds((int) s.getMaxInactiveInterval().getSeconds())
                        .build())
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void revokeSession(MemberId memberId, String sessionId) {
        var key = memberId.id().toString();

        var sessions = sessionRepository.findByIndexNameAndIndexValue(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                key
        );
        if (!sessions.containsKey(sessionId)) {
            // 본인 소유 아님 or 없음 → 조용히 무시하거나 404로 바꿔도 됨
            return;
        }

        sessionRepository.deleteById(sessionId);
    }
}
