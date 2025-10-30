package com.y11i.springcommddd.web.iam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.y11i.springcommddd.iam.api.EmailVerificationController;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMemberPrincipal;
import com.y11i.springcommddd.iam.api.support.CurrentMemberIdArgumentResolver;
import com.y11i.springcommddd.iam.application.port.in.EmailVerificationUseCase;
import com.y11i.springcommddd.iam.domain.MemberId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EmailVerificationController의 Web 계층 슬라이스 테스트.
 *
 * 다루는 엔드포인트:
 *  - POST /api/email-verify/signup/request      (로그인 필요)
 *  - POST /api/email-verify/signup/confirm      (비로그인 허용)
 *  - POST /api/email-verify/change/request      (로그인 필요)
 *  - POST /api/email-verify/change/confirm      (비로그인 허용)
 *
 * 검증 포인트:
 *  - 정상 입력 시 HTTP 상태코드 (202 / 204)
 *  - @Valid 위반 시 400 + ProblemDetail
 *  - 인증 누락 시 401 + ProblemDetail
 *  - UseCase가 올바른 파라미터(UUID, token 등)로 호출되는지
 */
@WebMvcTest(controllers = EmailVerificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(EmailVerificationControllerWebTest.TestWebConfig.class)
class EmailVerificationControllerWebTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EmailVerificationUseCase emailVerificationUseCase;

    MemberId memberId;
    AuthenticatedMemberPrincipal principal;

    /**
     * 테스트 컨텍스트에서 @AuthenticatedMember MemberId 주입을 재현하기 위해
     * CurrentMemberIdArgumentResolver를 수동 등록한다.
     */
    @TestConfiguration
    static class TestWebConfig implements WebMvcConfigurer {
        @Bean
        CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver() {
            return new CurrentMemberIdArgumentResolver();
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(currentMemberIdArgumentResolver());
        }
    }

    @BeforeEach
    void setUpSecurityContext() {
        // 실제 도메인 타입: record MemberId(UUID id)
        memberId = new MemberId(UUID.randomUUID());

        // 실제 principal 시그니처:
        // new AuthenticatedMemberPrincipal(memberId, email, authorities, passwordHash)
        principal = new AuthenticatedMemberPrincipal(
                memberId,
                "user@example.com",
                Set.of(),                 // authorities
                "encoded-password-hash"   // passwordHash
        );

        var auth = new TestingAuthenticationToken(
                principal,
                principal.getPassword(),       // credentials = passwordHash
                principal.getAuthorities()     // authorities = Set<? extends GrantedAuthority>
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // -----------------------------------------------------------------------
    // 1) POST /api/email-verify/signup/request  (로그인 필요)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/email-verify/signup/request - 로그인 상태에서 올바른 이메일이면 202 Accepted를 반환하고 requestForSignup(memberId.id, email)을 호출한다")
    void requestForSignupMe_authenticated_validEmail_returns202_andInvokesUseCase() throws Exception {
        var bodyJson = """
            {
              "email": "newuser@example.com"
            }
        """;

        mockMvc.perform(post("/api/email-verify/signup/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
                )
                .andExpect(status().isAccepted());

        verify(emailVerificationUseCase).requestForSignup(
                eq(memberId.id()),          // UUID
                eq("newuser@example.com")
        );
    }

    @Test
    @DisplayName("POST /api/email-verify/signup/request - 잘못된 이메일 형식이면 400 Bad Request와 ProblemDetail을 반환한다")
    void requestForSignupMe_invalidEmail_returns400_withProblemDetail() throws Exception {
        var bodyJson = """
            {
              "email": "not-an-email"
            }
        """;

        mockMvc.perform(post("/api/email-verify/signup/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    @DisplayName("POST /api/email-verify/signup/request - 인증 정보가 없으면 401 Unauthorized와 ProblemDetail을 반환한다")
    void requestForSignupMe_unauthenticated_returns401_withProblemDetail() throws Exception {
        SecurityContextHolder.clearContext(); // 인증 제거

        var bodyJson = """
            {
              "email": "newuser@example.com"
            }
        """;

        mockMvc.perform(post("/api/email-verify/signup/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("app.permission_denied"))
                .andExpect(jsonPath("$.detail").value("Not Authenticated"));
    }

    // -----------------------------------------------------------------------
    // 2) POST /api/email-verify/signup/confirm  (로그인 불필요)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/email-verify/signup/confirm - 유효한 token이면 204 No Content를 반환하고 confirmSignup(token)이 호출된다")
    void confirmSignUp_validToken_returns204_andInvokesUseCase() throws Exception {
        var bodyJson = """
            {
              "token": "SIGNUP_TOKEN_ABC"
            }
        """;

        mockMvc.perform(post("/api/email-verify/signup/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
                )
                .andExpect(status().isNoContent());

        verify(emailVerificationUseCase).confirmSignup("SIGNUP_TOKEN_ABC");
    }

    @Test
    @DisplayName("POST /api/email-verify/signup/confirm - token이 공백이면 400 Bad Request와 ProblemDetail을 반환한다")
    void confirmSignUp_blankToken_returns400_withProblemDetail() throws Exception {
        var bodyJson = """
            {
              "token": ""
            }
        """;

        mockMvc.perform(post("/api/email-verify/signup/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.detail").exists());
    }

    // -----------------------------------------------------------------------
    // 3) POST /api/email-verify/change/request  (로그인 필요)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/email-verify/change/request - 로그인 상태에서 새 이메일을 주면 202 Accepted를 반환하고 requestForChange(memberId.id, email)이 호출된다")
    void requestForChange_authenticated_validEmail_returns202_andInvokesUseCase() throws Exception {
        var bodyJson = """
            {
              "email": "changed@example.com"
            }
        """;

        mockMvc.perform(post("/api/email-verify/change/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
                )
                .andExpect(status().isAccepted());

        verify(emailVerificationUseCase).requestForChange(
                eq(memberId.id()),
                eq("changed@example.com")
        );
    }

    @Test
    @DisplayName("POST /api/email-verify/change/request - 잘못된 이메일 형식이면 400 Bad Request와 ProblemDetail을 반환한다")
    void requestForChange_invalidEmail_returns400_withProblemDetail() throws Exception {
        var bodyJson = """
            {
              "email": "bad"
            }
        """;

        mockMvc.perform(post("/api/email-verify/change/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    @DisplayName("POST /api/email-verify/change/request - 인증 정보가 없으면 401 Unauthorized와 ProblemDetail을 반환한다")
    void requestForChange_unauthenticated_returns401_withProblemDetail() throws Exception {
        SecurityContextHolder.clearContext(); // 인증 제거

        var bodyJson = """
            {
              "email": "changed@example.com"
            }
        """;

        mockMvc.perform(post("/api/email-verify/change/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("app.permission_denied"))
                .andExpect(jsonPath("$.detail").value("Not Authenticated"));
    }

    // -----------------------------------------------------------------------
    // 4) POST /api/email-verify/change/confirm  (로그인 불필요)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/email-verify/change/confirm - 유효한 token이면 204 No Content를 반환하고 confirmChange(token)이 호출된다")
    void confirmChange_validToken_returns204_andInvokesUseCase() throws Exception {
        var bodyJson = """
            {
              "token": "CHANGE_TOKEN_123"
            }
        """;

        mockMvc.perform(post("/api/email-verify/change/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
                )
                .andExpect(status().isNoContent());

        verify(emailVerificationUseCase).confirmChange("CHANGE_TOKEN_123");
    }

    @Test
    @DisplayName("POST /api/email-verify/change/confirm - token이 공백이면 400 Bad Request와 ProblemDetail을 반환한다")
    void confirmChange_blankToken_returns400_withProblemDetail() throws Exception {
        var bodyJson = """
            {
              "token": ""
            }
        """;

        mockMvc.perform(post("/api/email-verify/change/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.detail").exists());
    }
}
