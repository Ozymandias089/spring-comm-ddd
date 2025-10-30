package com.y11i.springcommddd.unit.iam;

import com.y11i.springcommddd.iam.application.service.SessionManagementService;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.SessionDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * SessionManagementService 단위 테스트.
 *
 * 이 서비스는 세션 저장소(Spring Session repository)를 포트처럼 사용하고 있고,
 * 사용자의 MemberId(UUID)로 세션을 조회/삭제하는 애플리케이션 서비스 로직을 담당한다.
 *
 * 여기서 검증하려는 핵심 비즈니스 규칙:
 * 1) listMySessions(MemberId):
 *    - 현재 사용자 UUID로 인덱싱된 세션들을 조회한다.
 *    - Session -> SessionDTO 매핑이 올바른가? (ISO 시각 문자열, 만료시간 등)
 *
 * 2) revokeSession(MemberId, sessionId):
 *    - 해당 사용자의 세션 목록에 그 sessionId가 실제로 존재할 때만 삭제한다.
 *    - 남의 세션/없는 세션이면 조용히 무시한다. (예외를 던지지 않는다)
 */
@ExtendWith(MockitoExtension.class)
class SessionManagementServiceTest {

    @Test
    @DisplayName("listMySessions() - 사용자의 세션 정보를 조회하면 SessionDTO 리스트로 매핑되어 반환된다")
    void listMySessions_returnsMappedDtos() {
        // given
        @SuppressWarnings("unchecked")
        FindByIndexNameSessionRepository<Session> repo = mock(FindByIndexNameSessionRepository.class);

        var service = new SessionManagementService(repo);

        UUID uuid = UUID.randomUUID();
        var memberId = new MemberId(uuid);

        // mock session
        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn("sess-123");
        when(mockSession.getCreationTime()).thenReturn(Instant.parse("2025-10-30T12:00:00Z"));
        when(mockSession.getLastAccessedTime()).thenReturn(Instant.parse("2025-10-30T13:00:00Z"));
        when(mockSession.getMaxInactiveInterval()).thenReturn(Duration.ofSeconds(3600));

        // repo behavior: find all sessions indexed by that user's UUID string
        when(repo.findByIndexNameAndIndexValue(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                uuid.toString()
        )).thenReturn(Map.of("sess-123", mockSession));

        // when
        var result = service.listMySessions(memberId);

        // then
        assertThat(result).hasSize(1);

        SessionDTO dto = result.get(0);
        assertThat(dto.getSessionId()).isEqualTo("sess-123");
        // SessionManagementService는 DateTimeFormatter.ISO_INSTANT 로 문자열화하므로, 끝에 'Z' 포함된 UTC ISO-8601 문자열이어야 한다.
        assertThat(dto.getCreationTime()).isEqualTo("2025-10-30T12:00:00Z");
        assertThat(dto.getLastAccessedTime()).isEqualTo("2025-10-30T13:00:00Z");
        assertThat(dto.getMaxInactiveIntervalSeconds()).isEqualTo(3600);

        // verify repo interaction
        verify(repo).findByIndexNameAndIndexValue(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                uuid.toString()
        );
    }

    @Test
    @DisplayName("revokeSession() - 내가 소유한 세션 ID를 주면 해당 세션이 삭제된다")
    void revokeSession_deletesWhenOwned() {
        // given
        @SuppressWarnings("unchecked")
        FindByIndexNameSessionRepository<Session> repo = mock(FindByIndexNameSessionRepository.class);
        var service = new SessionManagementService(repo);

        UUID uuid = UUID.randomUUID();
        var memberId = new MemberId(uuid);

        // 세션 저장소에 'abc' 라는 세션이 이 사용자 소유로 있다고 가정
        when(repo.findByIndexNameAndIndexValue(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                uuid.toString()
        )).thenReturn(Map.of("abc", mock(Session.class)));

        // when
        service.revokeSession(memberId, "abc");

        // then
        verify(repo).deleteById("abc");
    }

    @Test
    @DisplayName("revokeSession() - 내가 소유하지 않은 세션 ID면 아무 것도 삭제하지 않는다 (조용히 무시)")
    void revokeSession_ignoresWhenNotOwned() {
        // given
        @SuppressWarnings("unchecked")
        FindByIndexNameSessionRepository<Session> repo = mock(FindByIndexNameSessionRepository.class);
        var service = new SessionManagementService(repo);

        UUID uuid = UUID.randomUUID();
        var memberId = new MemberId(uuid);

        // repo에는 'abc'만 있고, 'zzz'는 없다.
        when(repo.findByIndexNameAndIndexValue(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                uuid.toString()
        )).thenReturn(Map.of("abc", mock(Session.class)));

        // when
        service.revokeSession(memberId, "zzz"); // 내가 가진 세션이 아님

        // then
        verify(repo, never()).deleteById(any());
    }
}
