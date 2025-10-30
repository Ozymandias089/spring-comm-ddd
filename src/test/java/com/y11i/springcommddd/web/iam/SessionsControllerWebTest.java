package com.y11i.springcommddd.web.iam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.y11i.springcommddd.iam.api.SessionsController;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMemberPrincipal;
import com.y11i.springcommddd.iam.application.port.in.SessionManagementUseCase;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.SessionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SessionsController 의 Web 계층 슬라이스 테스트.
 *
 * 커버 대상 엔드포인트:
 *  - GET /api/sessions           : 내 세션 목록 조회
 *  - DELETE /api/sessions/{id}   : 특정 세션 강제 종료
 *
 * 검증 포인트:
 *  - 정상 요청 시 올바른 HTTP 상태코드와 응답 구조
 *  - 인증 누락 시 401 ProblemDetail
 */
@WebMvcTest(controllers = SessionsController.class)
@AutoConfigureMockMvc(addFilters = false)
class SessionsControllerWebTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    SessionManagementUseCase sessionManagementUseCase;

    MemberId memberId;
    AuthenticatedMemberPrincipal principal;

    @BeforeEach
    void setUpSecurityContext() {
        memberId = new MemberId(UUID.randomUUID());
        principal = new AuthenticatedMemberPrincipal(
                memberId,
                "user@example.com",
                Set.of(),
                "encoded-password-hash"
        );
        var auth = new TestingAuthenticationToken(
                principal,
                principal.getPassword(),
                principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ---------------------------------------------------------------------
    // GET /api/sessions
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/sessions - 로그인 상태에서 내 세션 목록을 조회하면 200 OK와 세션 배열 JSON을 반환한다")
    void listSessions_authenticated_returns200_withJsonArray() throws Exception {
        var dto1 = new SessionDTO("sess-1", "2025-10-30T10:00:00Z", "2025-10-30T10:10:00Z", 1800);
        var dto2 = new SessionDTO("sess-2", "2025-10-29T09:00:00Z", "2025-10-30T09:45:00Z", 1800);
        when(sessionManagementUseCase.listMySessions(memberId))
                .thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sessionId").value("sess-1"))
                .andExpect(jsonPath("$[1].sessionId").value("sess-2"))
                .andExpect(jsonPath("$[0].maxInactiveIntervalSeconds").value(1800));

        verify(sessionManagementUseCase).listMySessions(memberId);
    }

    @Test
    @DisplayName("GET /api/sessions - 인증 정보가 없으면 401 Unauthorized와 ProblemDetail을 반환한다")
    void listSessions_unauthenticated_returns401ProblemDetail() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/sessions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("app.permission_denied"))
                .andExpect(jsonPath("$.detail").value("Not authenticated"));
    }

    // ---------------------------------------------------------------------
    // DELETE /api/sessions/{id}
    // ---------------------------------------------------------------------

    @Test
    @DisplayName("DELETE /api/sessions/{id} - 로그인 상태에서 세션을 종료하면 204 No Content를 반환하고 revokeSession(memberId, id)가 호출된다")
    void revokeSession_authenticated_returns204_andInvokesUseCase() throws Exception {
        mockMvc.perform(delete("/api/sessions/{sessionId}", "sess-123"))
                .andExpect(status().isNoContent());

        verify(sessionManagementUseCase).revokeSession(eq(memberId), eq("sess-123"));
    }

    @Test
    @DisplayName("DELETE /api/sessions/{id} - 인증 정보가 없으면 401 Unauthorized와 ProblemDetail을 반환한다")
    void revokeSession_unauthenticated_returns401ProblemDetail() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(delete("/api/sessions/{sessionId}", "sess-123"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("app.permission_denied"))
                .andExpect(jsonPath("$.detail").value("Not authenticated"));
    }
}
