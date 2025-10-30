package com.y11i.springcommddd.web.iam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.y11i.springcommddd.iam.api.AdminMemberController;
import com.y11i.springcommddd.iam.application.port.in.AdminMemberUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminMemberController 의 Web 계층 슬라이스 테스트.
 *
 * 이 컨트롤러는 관리자 전용 API라고 가정하지만,
 * 여기서는 시큐리티 필터를 비활성화(addFilters=false)했기 때문에
 * 인가/권한 검증은 다루지 않고 HTTP 계약만 검증한다.
 *
 * 검증 범위:
 * - @Valid 통과 시 상태코드 (204 / 201) 및 유스케이스 호출 여부
 * - @Valid 실패 시 400 ProblemDetail 구조
 * - create-admin 은 Location 헤더에 UUID가 포함되는지 확인
 */
@WebMvcTest(controllers = AdminMemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminMemberControllerWebTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AdminMemberUseCase adminMemberUseCase;

    // ----------------------------------------------------------------------
    // /grant-admin
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/admin/members/grant-admin - 유효한 memberId면 204 No Content를 반환하고 grantAdmin(...)이 호출된다")
    void grantAdmin_valid_returns204_andInvokesUseCase() throws Exception {
        var someUserId = UUID.randomUUID().toString();
        var bodyJson = """
            {
              "memberId": "%s"
            }
        """.formatted(someUserId);

        mockMvc.perform(
                        post("/api/admin/members/grant-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyJson)
                )
                .andExpect(status().isNoContent());

        // 유스케이스 호출 여부 확인
        verify(adminMemberUseCase).grantAdmin(any(AdminMemberUseCase.GrantAdminCommand.class));
    }

    @Test
    @DisplayName("POST /api/admin/members/grant-admin - memberId가 null이면 400 Bad Request와 ProblemDetail을 반환한다")
    void grantAdmin_invalid_returns400_withProblemDetail() throws Exception {
        // memberId 누락(null) 시나리오
        var bodyJson = """
            {
              "memberId": null
            }
        """;

        mockMvc.perform(
                        post("/api/admin/members/grant-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.detail").exists());
    }

    // ----------------------------------------------------------------------
    // /revoke-admin
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/admin/members/revoke-admin - 유효한 memberId면 204 No Content를 반환하고 revokeAdmin(...)이 호출된다")
    void revokeAdmin_valid_returns204_andInvokesUseCase() throws Exception {
        var someUserId = UUID.randomUUID().toString();
        var bodyJson = """
            {
              "memberId": "%s"
            }
        """.formatted(someUserId);

        mockMvc.perform(
                        post("/api/admin/members/revoke-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyJson)
                )
                .andExpect(status().isNoContent());

        verify(adminMemberUseCase).revokeAdmin(any(AdminMemberUseCase.RevokeAdminCommand.class));
    }

    @Test
    @DisplayName("POST /api/admin/members/revoke-admin - memberId가 null이면 400 Bad Request와 ProblemDetail을 반환한다")
    void revokeAdmin_invalid_returns400_withProblemDetail() throws Exception {
        var bodyJson = """
            {
              "memberId": null
            }
        """;

        mockMvc.perform(
                        post("/api/admin/members/revoke-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.detail").exists());
    }

    // ----------------------------------------------------------------------
    // /set-status
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/admin/members/set-status - 유효한 memberId와 status면 204 No Content를 반환하고 setStatus(...)가 호출된다")
    void setStatus_valid_returns204_andInvokesUseCase() throws Exception {
        var targetId = UUID.randomUUID().toString();
        var bodyJson = """
            {
              "memberId": "%s",
              "status": "SUSPENDED"
            }
        """.formatted(targetId);

        mockMvc.perform(
                        post("/api/admin/members/set-status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyJson)
                )
                .andExpect(status().isNoContent());

        verify(adminMemberUseCase).setStatus(any(AdminMemberUseCase.SetStatusCommand.class));
    }

    @Test
    @DisplayName("POST /api/admin/members/set-status - 잘못된 status나 null memberId면 400 Bad Request와 ProblemDetail을 반환한다")
    void setStatus_invalid_returns400_withProblemDetail() throws Exception {
        // status = BANISHED (허용 regexp = ACTIVE|SUSPENDED|DELETED)
        // memberId = null
        var bodyJson = """
            {
              "memberId": null,
              "status": "BANISHED"
            }
        """;

        mockMvc.perform(
                        post("/api/admin/members/set-status")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyJson)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.detail").exists());
    }

    // ----------------------------------------------------------------------
    // /create-admin
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/admin/members/create-admin - 유효한 정보면 201 Created를 반환하고 Location 헤더에 생성된 관리자 계정 URI가 담긴다")
    void createAdmin_valid_returns201Created_withLocation_andInvokesUseCase() throws Exception {
        var newAdminId = UUID.randomUUID();
        when(adminMemberUseCase.createAdminAccount(any(AdminMemberUseCase.CreateAdminCommand.class)))
                .thenReturn(newAdminId);

        var bodyJson = """
            {
              "email": "admin@example.com",
              "displayName": "Super Admin",
              "password": "UltraStrongPass!23"
            }
        """;

        mockMvc.perform(
                        post("/api/admin/members/create-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(bodyJson)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/admin/members/" + newAdminId));

        // 캡처해서 실제로 어떤 Command가 전달됐는지 검증해볼 수도 있다.
        var captor = ArgumentCaptor.forClass(AdminMemberUseCase.CreateAdminCommand.class);
        verify(adminMemberUseCase).createAdminAccount(captor.capture());
        var command = captor.getValue();

        assertThat(command.email()).isEqualTo("admin@example.com");
        assertThat(command.displayName()).isEqualTo("Super Admin");
        assertThat(command.rawPassword()).isEqualTo("UltraStrongPass!23");
    }

    @Test
    @DisplayName("POST /api/admin/members/create-admin - 잘못된 요청(이메일 형식 불량, displayName 너무 짧음, 비밀번호 너무 짧음)이면 400 Bad Request와 ProblemDetail을 반환한다")
    void createAdmin_invalid_returns400_withProblemDetail() throws Exception {
        // email: not valid
        // displayName: "" (NotBlank, Size(min=2))
        // password: "123" (Size(min=8))
        var bodyJson = """
            {
              "email": "not-an-email",
              "displayName": "",
              "password": "123"
            }
        """;

        mockMvc.perform(
                        post("/api/admin/members/create-admin")
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
