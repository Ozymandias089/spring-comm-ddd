package com.y11i.springcommddd.iam.application.service;

import com.y11i.springcommddd.iam.application.port.in.SessionManagementUseCase;
import com.y11i.springcommddd.iam.dto.SessionDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionManagementService implements SessionManagementUseCase {

    private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    @Override
    @Transactional(readOnly = true)
    public List<SessionDTO> listMySessions(String principalEmail) {
        return sessionRepository
                .findByIndexNameAndIndexValue(
                        FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, // ← 문자열 상수 사용
                        principalEmail
                )
                .values()
                .stream()
                .map(s -> SessionDTO.builder()
                        .sessionId(s.getId())
                        // s.getCreationTime(), getLastAccessedTime()은 이미 Instant
                        .creationTime(ISO.format(s.getCreationTime()))
                        .lastAccessedTime(ISO.format(s.getLastAccessedTime()))
                        .maxInactiveIntervalSeconds((int) s.getMaxInactiveInterval().getSeconds())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void revokeSession(String principalEmail, String sessionId) {
        var sessions = sessionRepository.findByIndexNameAndIndexValue(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                principalEmail
        );
        if (!sessions.containsKey(sessionId)) {
            // 본인 소유 아님 or 없음 → 조용히 무시하거나 404로 바꿔도 됨
            return;
        }
    }
}
