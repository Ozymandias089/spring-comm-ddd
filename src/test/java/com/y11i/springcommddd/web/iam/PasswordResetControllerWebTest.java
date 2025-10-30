package com.y11i.springcommddd.web.iam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.y11i.springcommddd.iam.api.PasswordResetController;
import com.y11i.springcommddd.iam.application.port.in.PasswordResetUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PasswordResetController 의 Web 계층 슬라이스 테스트.
 * <p>
 * 특징:
 * - 인증 불필요 (로그아웃된 사용자가 "비번 재설정" 요청할 수 있어야 하므로)
 * - 유효성 실패 시 400 ProblemDetail
 * - 정상 시
 *   - /api/password-reset           -> 202 Accepted
 *   - /api/password-reset/confirm   -> 204 No Content
 */
@WebMvcTest(controllers = PasswordResetController.class)
@AutoConfigureMockMvc(addFilters = false)
class PasswordResetControllerWebTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PasswordResetUseCase passwordResetUseCase;

    // ------------------------------------------------------
    // POST /api/password-reset
    // ------------------------------------------------------

    @Test
    @DisplayName("POST /api/password-reset - 올바른 이메일이면 202 Accepted를 반환하고 useCase.request(email)이 호출된다")
    void requestPasswordReset_validEmail_returns202_andInvokesUseCase() throws Exception {
        var bodyJson = """
            {
              "email": "lostuser@example.com"
            }
        """;

        mockMvc.perform(
                        post("/api/password-reset")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyJson)
                )
                .andExpect(status().isAccepted());

        verify(passwordResetUseCase).request(eq("lostuser@example.com"));
    }

    @Test
    @DisplayName("POST /api/password-reset - 잘못된 이메일이면 400 Bad Request와 ProblemDetail을 반환한다")
    void requestPasswordReset_invalidEmail_returns400_withProblemDetail() throws Exception {
        var bodyJson = """
            {
              "email": "not-an-email"
            }
        """;

        mockMvc.perform(
                        post("/api/password-reset")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.detail").exists());
    }

    // ------------------------------------------------------
    // POST /api/password-reset/confirm
    // ------------------------------------------------------

    @Test
    @DisplayName("POST /api/password-reset/confirm - 유효한 token과 새 비밀번호면 204 No Content를 반환하고 useCase.confirm(token, newPassword)가 호출된다")
    void confirmPasswordReset_validRequest_returns204_andInvokesUseCase() throws Exception {
        var bodyJson = """
            {
              "token": "RESET_TOKEN_ABC",
              "newPassword": "StrongPassword123!"
            }
        """;

        mockMvc.perform(
                        post("/api/password-reset/confirm")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyJson)
                )
                .andExpect(status().isNoContent());

        verify(passwordResetUseCase).confirm(
                eq("RESET_TOKEN_ABC"),
                eq("StrongPassword123!")
        );
    }

    @Test
    @DisplayName("POST /api/password-reset/confirm - token이 비어있거나 비밀번호가 너무 짧으면 400 Bad Request와 ProblemDetail을 반환한다")
    void confirmPasswordReset_invalidRequest_returns400_withProblemDetail() throws Exception {
        // token = ""  -> @NotBlank 위반
        // newPassword = "123" -> @Size(min=8) 위반
        var bodyJson = """
            {
              "token": "",
              "newPassword": "123"
            }
        """;

        mockMvc.perform(
                        post("/api/password-reset/confirm")
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
