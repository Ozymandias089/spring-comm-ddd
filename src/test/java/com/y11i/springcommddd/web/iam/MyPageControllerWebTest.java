package com.y11i.springcommddd.web.iam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.y11i.springcommddd.iam.api.MyPageController;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMemberPrincipal;
import com.y11i.springcommddd.iam.api.support.CurrentMemberIdArgumentResolver;
import com.y11i.springcommddd.iam.application.port.in.FindMemberUseCase;
import com.y11i.springcommddd.iam.application.port.in.ManageProfileUseCase;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.MemberDTO;
import com.y11i.springcommddd.iam.dto.request.ChangeBannerImageRequestDTO;
import com.y11i.springcommddd.iam.dto.request.ChangeEmailRequestDTO;
import com.y11i.springcommddd.iam.dto.request.ChangeProfileImageRequestDTO;
import com.y11i.springcommddd.iam.dto.request.RenameRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MyPageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MyPageControllerWebTest.ResolverConfig.class)
class MyPageControllerWebTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean FindMemberUseCase findMemberUseCase;
    @MockBean ManageProfileUseCase manageProfileUseCase;

    UUID uuid;
    MemberId memberId;
    AuthenticatedMemberPrincipal principal;

    /**
     * 테스트 전용 MVC 설정.
     * - CurrentMemberIdArgumentResolver 빈을 직접 만든다.
     * - 그 리졸버를 MVC에 등록한다.
     * <p>
     * 주의: 여기서는 실제 애플리케이션 컨텍스트에서 동일 이름 빈이 안 올라오기 때문에
     * 충돌이 없다.
     */
    @TestConfiguration
    static class ResolverConfig implements WebMvcConfigurer {

        @Bean
        CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver() {
            return new CurrentMemberIdArgumentResolver();
        }

        @Override
        public void addArgumentResolvers(java.util.List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(currentMemberIdArgumentResolver());
        }
    }

    @BeforeEach
    void setUpSecurityContext() {
        uuid = UUID.randomUUID();
        memberId = new MemberId(uuid);

        principal = new AuthenticatedMemberPrincipal(
                memberId,
                "me@example.com",
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                "{noop}pw"
        );

        var auth = new TestingAuthenticationToken(
                principal,
                null,
                "ROLE_USER"
        );
        auth.setAuthenticated(true);

        SecurityContextHolder.setContext(new SecurityContextImpl(auth));
    }

    @Test
    @DisplayName("GET /api/my-page 은 현재 인증된 사용자의 정보를 MyPageResponseDTO로 반환한다")
    void me_returnsMyProfile() throws Exception {

        MemberDTO dto = MemberDTO.builder()
                .memberId(memberId)
                .email("me@example.com")
                .displayName("my-nickname")
                .roles(Set.of("USER"))
                .status("ACTIVE")
                .passwordResetRequired(false)
                .emailVerified(true)
                .build();

        given(findMemberUseCase.findById(uuid)).willReturn(Optional.of(dto));

        mockMvc.perform(
                        get("/api/my-page")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(uuid.toString()))
                .andExpect(jsonPath("$.email").value("me@example.com"))
                .andExpect(jsonPath("$.displayName").value("my-nickname"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.emailVerified").value(true));
    }

    @Test
    @DisplayName("PATCH /api/my-page/display-name 은 본인 닉네임을 변경하고 변경된 프로필을 반환한다")
    void rename_updatesDisplayName() throws Exception {
        RenameRequestDTO body = new RenameRequestDTO("new-nickname");

        MemberDTO updated = MemberDTO.builder()
                .memberId(memberId)
                .email("me@example.com")
                .displayName("new-nickname")
                .roles(Set.of("USER"))
                .status("ACTIVE")
                .passwordResetRequired(false)
                .emailVerified(true)
                .build();

        given(manageProfileUseCase.rename(any(ManageProfileUseCase.RenameCommand.class)))
                .willReturn(updated);

        mockMvc.perform(
                        patch("/api/my-page/display-name")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("new-nickname"))
                .andExpect(jsonPath("$.memberId").value(uuid.toString()))
                .andExpect(jsonPath("$.email").value("me@example.com"));
    }

    @Test
    @DisplayName("PATCH /api/my-page/email 은 이메일을 변경하고 변경된 프로필을 반환한다")
    void changeEmail_updatesEmail() throws Exception {
        // given
        ChangeEmailRequestDTO body = new ChangeEmailRequestDTO("new-email@example.com");

        MemberDTO updated = MemberDTO.builder()
                .memberId(memberId)
                .email("new-email@example.com")
                .displayName("my-nickname")
                .roles(Set.of("USER"))
                .status("ACTIVE")
                .passwordResetRequired(false)
                .emailVerified(true)
                .build();

        given(manageProfileUseCase.changeEmail(
                any(ManageProfileUseCase.ChangeEmailCommand.class)
        )).willReturn(updated);

        mockMvc.perform(
                        patch("/api/my-page/email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(uuid.toString()))
                .andExpect(jsonPath("$.email").value("new-email@example.com"))
                .andExpect(jsonPath("$.displayName").value("my-nickname"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.emailVerified").value(true));
    }

    @Test
    @DisplayName("PATCH /api/my-page/profile-image 은 프로필 이미지를 변경하고 변경된 프로필을 반환한다")
    void changeProfileImage_updatesProfileImage() throws Exception {
        ChangeProfileImageRequestDTO body = new ChangeProfileImageRequestDTO(
                "https://cdn.example.com/profile/me.png"
        );

        MemberDTO updated = MemberDTO.builder()
                .memberId(memberId)
                .email("me@example.com")
                .displayName("my-nickname")
                .roles(Set.of("USER"))
                .status("ACTIVE")
                .passwordResetRequired(false)
                .emailVerified(true)
                .profileImageUrl("https://cdn.example.com/profile/me.png")
                .build();

        given(manageProfileUseCase.changeProfileImage(
                any(ManageProfileUseCase.ChangeProfileImageCommand.class)
        )).willReturn(updated);

        mockMvc.perform(
                        patch("/api/my-page/profile-image")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(uuid.toString()))
                .andExpect(jsonPath("$.profileImageUrl").value("https://cdn.example.com/profile/me.png"))
                .andExpect(jsonPath("$.email").value("me@example.com"))
                .andExpect(jsonPath("$.displayName").value("my-nickname"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.emailVerified").value(true));
    }

    @Test
    @DisplayName("PATCH /api/my-page/banner-image 은 배너 이미지를 변경하고 변경된 프로필을 반환한다")
    void changeBannerImage_updatesBannerImage() throws Exception {
        ChangeBannerImageRequestDTO body = new ChangeBannerImageRequestDTO(
                "https://cdn.example.com/banner/me-header.png"
        );

        MemberDTO updated = MemberDTO.builder()
                .memberId(memberId)
                .email("me@example.com")
                .displayName("my-nickname")
                .roles(Set.of("USER"))
                .status("ACTIVE")
                .passwordResetRequired(false)
                .emailVerified(true)
                .bannerImageUrl("https://cdn.example.com/banner/me-header.png")
                .build();

        given(manageProfileUseCase.changeBannerImage(
                any(ManageProfileUseCase.ChangeBannerImageCommand.class)
        )).willReturn(updated);

        mockMvc.perform(
                        patch("/api/my-page/banner-image")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(uuid.toString()))
                .andExpect(jsonPath("$.bannerImageUrl").value("https://cdn.example.com/banner/me-header.png"))
                .andExpect(jsonPath("$.email").value("me@example.com"))
                .andExpect(jsonPath("$.displayName").value("my-nickname"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.emailVerified").value(true));
    }

    @Test
    @DisplayName("PATCH /api/my-page/display-name 은 잘못된 닉네임이면 400과 ProblemDetail을 반환한다")
    void rename_validationError_returns400() throws Exception {
        // invalid: 빈 문자열
        RenameRequestDTO body = new RenameRequestDTO("");

        mockMvc.perform(
                        patch("/api/my-page/display-name")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.code").value("generic.bad_request"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @Disabled
    @DisplayName("GET /api/my-page 은 인증 컨텍스트가 없으면 401 Unauthorized를 반환한다")
    void me_unauthenticated_returns401() throws Exception {
        // given: SecurityContextHolder 비우기
        SecurityContextHolder.clearContext();

        mockMvc.perform(
                        get("/api/my-page")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("app.permission_denied"))
                .andExpect(jsonPath("$.detail").value("Not Authenticated"));
    }

    @Test
    @DisplayName("GET /api/my-page 에서 현재 사용자를 찾을 수 없으면 5xx (임시 동작)를 낸다")
    void me_memberNotFound_currently500() throws Exception {
        // given: 정상 인증 컨텍스트는 유지
        // but findMemberUseCase returns empty()
        given(findMemberUseCase.findById(uuid)).willReturn(Optional.empty());

        mockMvc.perform(
                        get("/api/my-page")
                )
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("PATCH /api/my-page/email 은 잘못된 이메일이면 400과 ProblemDetail을 반환한다")
    void changeEmail_validationError_returns400() throws Exception {
        ChangeEmailRequestDTO body = new ChangeEmailRequestDTO("not-an-email");

        mockMvc.perform(
                        patch("/api/my-page/email")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Failed"))
                .andExpect(jsonPath("$.code").value("generic.bad_request"))
                .andExpect(jsonPath("$.errors").exists());
    }

}
