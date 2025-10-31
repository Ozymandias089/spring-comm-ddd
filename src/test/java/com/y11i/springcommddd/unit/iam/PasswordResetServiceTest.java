package com.y11i.springcommddd.unit.iam;

import com.y11i.springcommddd.iam.application.port.out.LoadMemberPort;
import com.y11i.springcommddd.iam.application.port.out.PasswordResetTokenPort;
import com.y11i.springcommddd.iam.application.port.out.SaveMemberPort;
import com.y11i.springcommddd.iam.application.service.PasswordResetService;
import com.y11i.springcommddd.iam.domain.Email;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PasswordResetService 단위 테스트
 * <p>
 * 커버 범위:
 * <p>
 * request(email):
 *  - 주어진 email로 가입된 사용자가 없으면 아무 것도 하지 않고 종료 (보안상 침묵)
 *  - 사용자가 있으면:
 *      - passwordResetTokenPort.issueToken(memberId, TTL=5min) 호출
 *      - (현재 구현은 메일이 아니라 log.info만 하므로 mailPort 상호작용 없음)
 * <p>
 * confirm(token, newPassword):
 *  - passwordResetTokenPort.consume(token) 호출
 *      - IllegalArgumentException 발생 시 -> ResponseStatusException(400, "Invalid token")
 *  - memberId로 loadMemberPort.loadById(...) 해서 사용자를 찾지 못하면 -> ResponseStatusException(400, "Invalid token")
 *  - 찾으면 passwordEncoder.encode(newPassword) 후 member.setNewPassword(encodedPw)
 *  - saveMemberPort.save(member) 호출
 */
@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Nested
    class RequestTests {

        @Test
        @DisplayName("request(): 해당 이메일의 사용자가 없으면 아무 것도 하지 않는다 (항상 202로 가정하는 침묵 정책)")
        void request_userNotFound_doesNothing() {
            // given
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordResetTokenPort tokenPort = mock(PasswordResetTokenPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            PasswordResetService sut = new PasswordResetService(
                    loadMemberPort, saveMemberPort, tokenPort, passwordEncoder
            );

            when(loadMemberPort.loadByEmail(any()))
                    .thenReturn(Optional.empty());

            // when
            sut.request("ghost@example.com");

            // then
            // loadByEmail()가 호출된 건 맞는지, 그때 Email VO 안에 올바른 값이 들어갔는지 확인
            ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
            verify(loadMemberPort).loadByEmail(captor.capture());
            assertThat(captor.getValue().value()).isEqualTo("ghost@example.com");

            // 사용자 없으므로 나머지 포트는 건드리지 않아야 함
            verifyNoInteractions(tokenPort);
            verifyNoInteractions(saveMemberPort);
            verifyNoInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("request(): 사용자가 존재하면 토큰을 발급한다 (로그인 링크 전송용)")
        void request_userExists_issuesToken() {
            // given
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordResetTokenPort tokenPort = mock(PasswordResetTokenPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            PasswordResetService sut = new PasswordResetService(
                    loadMemberPort, saveMemberPort, tokenPort, passwordEncoder
            );

            UUID memberUuid = UUID.randomUUID();
            MemberId memberId = new MemberId(memberUuid);

            Member mockMember = mock(Member.class);
            when(mockMember.memberId()).thenReturn(memberId);

            when(loadMemberPort.loadByEmail(any()))
                    .thenReturn(Optional.of(mockMember));

            when(tokenPort.issueToken(eq(memberUuid), any()))
                    .thenReturn("RESET_TOKEN_ABC");

            // when
            sut.request("user@example.com");

            // then
            // 1) loadByEmail(...) 호출 시 들어간 Email 값 검증
            ArgumentCaptor<Email> captor = ArgumentCaptor.forClass(Email.class);
            verify(loadMemberPort).loadByEmail(captor.capture());
            assertThat(captor.getValue().value()).isEqualTo("user@example.com");

            // 2) 토큰 발급은 멤버 UUID 기반으로 호출돼야 한다
            verify(tokenPort).issueToken(eq(memberUuid), any());

            // 3) 아직 저장(save)하거나 패스워드 인코딩할 단계는 아니다
            verifyNoInteractions(saveMemberPort);
            verifyNoInteractions(passwordEncoder);
        }
    }

    @Nested
    class ConfirmTests {

        @Test
        @DisplayName("confirm(): 토큰이 잘못되었거나 만료되면 400 BAD_REQUEST (ResponseStatusException) 을 던진다")
        void confirm_invalidToken_throws400() {
            // given
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordResetTokenPort tokenPort = mock(PasswordResetTokenPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            PasswordResetService sut = new PasswordResetService(
                    loadMemberPort, saveMemberPort, tokenPort, passwordEncoder
            );

            // consume()이 IllegalArgumentException을 던지면 -> ResponseStatusException(400,"Invalid token")
            when(tokenPort.consume("BAD_TOKEN"))
                    .thenThrow(new IllegalArgumentException("expired or invalid"));

            // when / then
            assertThatThrownBy(() ->
                    sut.confirm("BAD_TOKEN", "NewPass123!")
            )
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode().value()).isEqualTo(400);
                        assertThat(rse.getReason()).contains("Invalid token");
                    });

            verify(tokenPort).consume("BAD_TOKEN");

            // member 로드 시도조차 안 해야 하므로, 다른 포트들은 상호작용 없어야 한다
            verifyNoInteractions(loadMemberPort);
            verifyNoInteractions(saveMemberPort);
            verifyNoInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("confirm(): 토큰은 유효하지만 해당 멤버를 찾지 못하면 400 BAD_REQUEST 를 던진다")
        void confirm_memberNotFound_throws400() {
            // given
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordResetTokenPort tokenPort = mock(PasswordResetTokenPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            PasswordResetService sut = new PasswordResetService(
                    loadMemberPort, saveMemberPort, tokenPort, passwordEncoder
            );

            UUID memberUuid = UUID.randomUUID();
            when(tokenPort.consume("GOOD_TOKEN"))
                    .thenReturn(memberUuid);

            // 멤버를 못 찾는 경우
            when(loadMemberPort.loadById(new MemberId(memberUuid)))
                    .thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() ->
                    sut.confirm("GOOD_TOKEN", "StrongPass!234")
            )
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException rse = (ResponseStatusException) ex;
                        assertThat(rse.getStatusCode().value()).isEqualTo(400);
                        assertThat(rse.getReason()).contains("Invalid token");
                    });

            verify(tokenPort).consume("GOOD_TOKEN");
            verify(loadMemberPort).loadById(new MemberId(memberUuid));

            verifyNoInteractions(saveMemberPort);
            verifyNoInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("confirm(): 정상 토큰 + 존재하는 멤버라면 비밀번호를 인코딩해 저장한다")
        void confirm_success_setsEncodedPassword_andSaves() {
            // given
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordResetTokenPort tokenPort = mock(PasswordResetTokenPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            PasswordResetService sut = new PasswordResetService(
                    loadMemberPort, saveMemberPort, tokenPort, passwordEncoder
            );

            UUID memberUuid = UUID.randomUUID();
            MemberId vo = new MemberId(memberUuid);
            Member mockMember = mock(Member.class);

            when(tokenPort.consume("VALID_TOKEN"))
                    .thenReturn(memberUuid);

            when(loadMemberPort.loadById(vo))
                    .thenReturn(Optional.of(mockMember));

            when(passwordEncoder.encode("NewPass#123"))
                    .thenReturn("ENCODED_!!!");

            // when
            sut.confirm("VALID_TOKEN", "NewPass#123");

            // then
            verify(tokenPort).consume("VALID_TOKEN");
            verify(loadMemberPort).loadById(vo);

            // 비밀번호 인코딩 후 setNewPassword 호출
            verify(passwordEncoder).encode("NewPass#123");
            verify(mockMember).setNewPassword("ENCODED_!!!");

            // 변경된 멤버를 저장
            verify(saveMemberPort).save(mockMember);
        }
    }
}
