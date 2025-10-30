package com.y11i.springcommddd.unit.iam;

import com.y11i.springcommddd.iam.application.service.SessionManagementService;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.SessionDTO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SessionManagementServiceTest {
    @Test
    void listMySessions_returnsMappedDtos() {
        // given
        @SuppressWarnings("unchecked")
        FindByIndexNameSessionRepository<Session> repo = mock(FindByIndexNameSessionRepository.class);

        var service = new SessionManagementService(repo);

        UUID uuid = UUID.randomUUID();
        var memberId = new MemberId(uuid);

        Session mockSession = mock(Session.class);
        when(mockSession.getId()).thenReturn("sess-123");
        when(mockSession.getCreationTime()).thenReturn(Instant.parse("2025-10-30T12:00:00Z"));
        when(mockSession.getLastAccessedTime()).thenReturn(Instant.parse("2025-10-30T13:00:00Z"));
        when(mockSession.getMaxInactiveInterval()).thenReturn(Duration.ofSeconds(3600));

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
        assertThat(dto.getCreationTime()).isEqualTo("2025-10-30T12:00:00Z");
        assertThat(dto.getLastAccessedTime()).isEqualTo("2025-10-30T13:00:00Z");
        assertThat(dto.getMaxInactiveIntervalSeconds()).isEqualTo(3600);
    }

    @Test
    void revokeSession_deletesOnlyIfOwned() {
        @SuppressWarnings("unchecked")
        FindByIndexNameSessionRepository<Session> repo = mock(FindByIndexNameSessionRepository.class);
        var service = new SessionManagementService(repo);

        UUID uuid = UUID.randomUUID();
        var memberId = new MemberId(uuid);

        // 내가 가진 세션 목록에 sessionId "abc"만 있다고 가정
        when(repo.findByIndexNameAndIndexValue(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                uuid.toString()
        )).thenReturn(Map.of("abc", mock(Session.class)));

        // when: 내가 가진 세션을 revoke
        service.revokeSession(memberId, "abc");

        // then: deleteById 호출됨
        verify(repo).deleteById("abc");

        // when: 남의 세션을 revoke 시도
        reset(repo);
        when(repo.findByIndexNameAndIndexValue(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                uuid.toString()
        )).thenReturn(Map.of("abc", mock(Session.class)));

        service.revokeSession(memberId, "zzz"); // not mine

        // then: deleteById 안 불러야 함
        verify(repo, never()).deleteById("zzz");
    }
}
