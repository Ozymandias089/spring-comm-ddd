package com.y11i.springcommddd.web.iam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.y11i.springcommddd.iam.api.MemberController;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMemberPrincipal;
import com.y11i.springcommddd.iam.application.port.in.FindMemberUseCase;
import com.y11i.springcommddd.iam.application.port.in.RegisterMemberUseCase;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.MemberDTO;
import com.y11i.springcommddd.iam.dto.request.LoginRequestDTO;
import com.y11i.springcommddd.iam.dto.request.RegisterRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MemberControllerWebTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    // === Controller dependencies ===
    @MockBean
    RegisterMemberUseCase registerMemberUseCase;

    @MockBean
    AuthenticationManager authenticationManager;

    @MockBean
    FindMemberUseCase findMemberUseCase;

    @Test
    @DisplayName("POST /api/login 성공 시 200 OK와 LoginResponseDTO 형태의 JSON을 반환한다")
    void login_success_returns200_andBody() throws Exception {
        // given
        // request body
        LoginRequestDTO req = new LoginRequestDTO("user@example.com", "pw1234abcd");

        // 가짜 멤버 id
        UUID uuid = UUID.randomUUID();
        MemberId memberId = new MemberId(uuid);

        // principal: 컨트롤러에서 기대하는 AuthenticatedMemberPrincipal
        var principal = new AuthenticatedMemberPrincipal(
                memberId,
                "user@example.com",
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                "{noop}hashed"
        );

        Authentication authResult = new UsernamePasswordAuthenticationToken(
                principal, // principal
                null,      // credentials
                principal.getAuthorities()
        );

        // AuthenticationManager.authenticate(...)가 항상 authResult를 돌려주도록 mock
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authResult);

        // 로그인 이후 컨트롤러는 findMemberUseCase.findById(uuid)를 호출한다
        MemberDTO dto = MemberDTO.builder()
                .memberId(memberId)
                .email("user@example.com")
                .displayName("test-user")
                .roles(Set.of("USER"))
                .status("ACTIVE")
                .passwordResetRequired(false)
                .emailVerified(true)
                .build();

        given(findMemberUseCase.findById(uuid)).willReturn(Optional.of(dto));

        // when + then
        mockMvc.perform(
                        post("/api/login")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                // ↓ LoginResponseDTO의 필드 구조에 맞춰서 검사
                .andExpect(jsonPath("$.memberId").value(uuid.toString()))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.displayName").value("test-user"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.passwordResetRequired").value(false));
    }

    @Test
    @DisplayName("POST /api/login 잘못된 요청 바디면 400(Bad Request) + GlobalExceptionHandler 포맷으로 응답한다")
    void login_validation_failure_returns400() throws Exception {
        // given: 비어있는 비밀번호 등 유효하지 않은 요청
        LoginRequestDTO badReq = new LoginRequestDTO("user@example.com", "");

        // when + then
        mockMvc.perform(
                        post("/api/login")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(badReq))
                )
                .andExpect(status().isBadRequest())
                // GlobalExceptionHandler가 ProblemDetail 형태를 내려주므로 대략적인 key 존재만 확인
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").exists())
                .andExpect(jsonPath("$.type").exists());
    }

    @Test
    @DisplayName("POST /api/register 성공 시 201 Created와 Location 헤더, RegisterResponseDTO JSON을 반환한다")
    void register_success_returns201_andBody() throws Exception {
        // given
        // 요청 바디 만들기 (RegisterRequestDTO 역시 생성자 없으면 테스트용 생성자 추가했겠지)
        RegisterRequestDTO req = new RegisterRequestDTO(
                "newuser@example.com",
                "new-user",
                "strongpassword123" // 비밀번호 정책 충족
        );

        UUID newUserId = UUID.randomUUID();
        MemberId memberId = new MemberId(newUserId);

        // registerMemberUseCase.register(...) 가 돌려줄 가짜 MemberDTO
        MemberDTO createdDto = MemberDTO.builder()
                .memberId(memberId)
                .email("newuser@example.com")
                .displayName("new-user")
                .roles(Set.of("USER"))
                .status("ACTIVE")
                .passwordResetRequired(false)
                .emailVerified(false)
                .build();

        given(registerMemberUseCase.register(any(RegisterMemberUseCase.Command.class)))
                .willReturn(createdDto);

        // when + then
        mockMvc.perform(
                        post("/api/register")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/members/" + newUserId.toString()))
                .andExpect(jsonPath("$.memberId").value(newUserId.toString()))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.displayName").value("new-user"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.passwordResetRequired").value(false));
    }
    @Test
    @DisplayName("POST /api/register 유효하지 않은 요청이면 400과 ProblemDetail을 반환한다")
    void register_validation_failure_returns400() throws Exception {
        // given: invalid displayName (예: 빈 문자열) 또는 너무 짧은 password 등
        RegisterRequestDTO badReq = new RegisterRequestDTO(
                "newuser@example.com",
                "",
                "short" // 정책 안 맞는 비밀번호라면 더 확실히 깨질 수 있음
        );

        mockMvc.perform(
                        post("/api/register")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(badReq))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.code").value("generic.bad_request"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("POST /api/logout 은 항상 204 No Content를 반환한다")
    void logout_always204() throws Exception {

        mockMvc.perform(
                        post("/api/logout")
                )
                .andExpect(status().isNoContent());
    }

}
