package com.y11i.springcommddd.unit.iam;

import com.y11i.springcommddd.iam.application.port.out.EmailVerificationTokenPort;
import com.y11i.springcommddd.iam.application.port.out.LoadMemberPort;
import com.y11i.springcommddd.iam.application.port.out.MailPort;
import com.y11i.springcommddd.iam.application.port.out.SaveMemberPort;
import com.y11i.springcommddd.iam.application.service.EmailVerificationService;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * EmailVerificationService 단위 테스트.
 * <p>
 * 커버 범위:
 * <p>
 * requestForSignup(memberId, email):
 *  - 멤버가 없으면 조용히 return (보안상 침묵)
 *  - 이미 emailVerified() == true면 아무 것도 안 하고 return (멱등)
 *  - 아직 미인증이면:
 *      - tokenPort.issueForSignup(...) 호출
 *      - MailPort.send(...) 호출
 * <p>
 * confirmSignup(token):
 *  - tokenPort.consumeForSignup(token)에서 IllegalArgumentException이면 400 BAD_REQUEST
 *  - member를 load했는데 없으면 400 BAD_REQUEST
 *  - 멤버가 아직 미인증이면 markEmailVerified() 후 saveMemberPort.save(member) 호출
 *  - 이미 인증된 멤버면 save 없이 조용히 통과 (멱등)
 *
 * <p>
 * requestForChange(memberId, newEmail):
 *  - 멤버 없으면 조용히 return
 *  - Email(newEmail) 생성으로 형식 검증 (여기서 예외가 나면 propagate)
 *  - tokenPort.issueForChange(...) 호출
 *  - MailPort.send(...) 호출
 * <p>
 * confirmChange(token):
 *  - tokenPort.consumeForChange(token)에서 IllegalArgumentException → 400 BAD_REQUEST
 *  - member 로드 실패 → 400 BAD_REQUEST
 *  - member.changeEmail(...) 호출
 *  - saveMemberPort.save(member) 호출
 */
@ExtendWith(MockitoExtension.class)
public class EmailVerificationServiceTest {
    // --- requestForSignup() -------------------------------------------------

    @Test
    @DisplayName("requestForSignup(): 멤버가 존재하지 않으면 아무 것도 하지 않고 조용히 종료한다")
    void requestForSignup_memberNotFound_doesNothing() {
        // given
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        EmailVerificationTokenPort tokenPort = mock(EmailVerificationTokenPort.class);
        MailPort mailPort = mock(MailPort.class);

        EmailVerificationService sut = new EmailVerificationService(
                loadMemberPort, saveMemberPort, tokenPort, mailPort
        );

        UUID memberUuid = UUID.randomUUID();
        when(loadMemberPort.loadById(new MemberId(memberUuid)))
                .thenReturn(Optional.empty());

        // when
        sut.requestForSignup(memberUuid, "user@example.com");

        // then
        verify(loadMemberPort).loadById(new MemberId(memberUuid));
        verifyNoInteractions(saveMemberPort);
        verifyNoInteractions(tokenPort);
        verifyNoInteractions(mailPort);
    }

    @Test
    @DisplayName("requestForSignup(): 이미 emailVerified == true 이면 토큰 발급/메일 전송 없이 조용히 종료한다")
    void requestForSignup_alreadyVerified_isNoOp() {
        // given
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        EmailVerificationTokenPort tokenPort = mock(EmailVerificationTokenPort.class);
        MailPort mailPort = mock(MailPort.class);

        EmailVerificationService sut = new EmailVerificationService(
                loadMemberPort, saveMemberPort, tokenPort, mailPort
        );

        UUID memberUuid = UUID.randomUUID();

        Member mockMember = mock(Member.class);
        when(mockMember.emailVerified()).thenReturn(true);
        lenient().when(mockMember.memberId()).thenReturn(new MemberId(memberUuid));

        when(loadMemberPort.loadById(new MemberId(memberUuid)))
                .thenReturn(Optional.of(mockMember));

        // when
        sut.requestForSignup(memberUuid, "user@example.com");

        // then
        verify(loadMemberPort).loadById(new MemberId(memberUuid));
        verify(mockMember).emailVerified();
        verifyNoInteractions(saveMemberPort);
        verifyNoInteractions(tokenPort);
        verifyNoInteractions(mailPort);
    }

    @Test
    @DisplayName("requestForSignup(): 아직 미인증이면 토큰 발급 후 MailPort.send()를 호출한다")
    void requestForSignup_notVerified_sendsEmail() {
        // given
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        EmailVerificationTokenPort tokenPort = mock(EmailVerificationTokenPort.class);
        MailPort mailPort = mock(MailPort.class);

        EmailVerificationService sut = new EmailVerificationService(
                loadMemberPort, saveMemberPort, tokenPort, mailPort
        );

        UUID memberUuid = UUID.randomUUID();
        MemberId memberIdVO = new MemberId(memberUuid);

        Member mockMember = mock(Member.class);
        when(mockMember.emailVerified()).thenReturn(false);
        when(mockMember.memberId()).thenReturn(memberIdVO);

        when(loadMemberPort.loadById(memberIdVO))
                .thenReturn(Optional.of(mockMember));

        when(tokenPort.issueForSignup(eq(memberUuid), any()))
                .thenReturn("SIGNUP_TOKEN_ABC");

        // when
        sut.requestForSignup(memberUuid, "user@example.com");

        // then
        verify(loadMemberPort).loadById(memberIdVO);
        verify(mockMember).emailVerified();

        verify(tokenPort).issueForSignup(eq(memberUuid), any());
        verify(mailPort).send(
                eq("user@example.com"),
                contains("Verify"), // subject "[SpringComm] Please Verify your Email"
                contains("SIGNUP_TOKEN_ABC")
        );

        // saveMemberPort.save(...)는 requestForSignup에서는 호출 안 함
        verifyNoInteractions(saveMemberPort);
    }

    // --- confirmSignup() ----------------------------------------------------

    @Test
    @DisplayName("confirmSignup(): 유효하지 않은/만료된 토큰이면 400 BAD_REQUEST를 던진다")
    void confirmSignup_invalidToken_throws400() {
        // given
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        EmailVerificationTokenPort tokenPort = mock(EmailVerificationTokenPort.class);
        MailPort mailPort = mock(MailPort.class);

        EmailVerificationService sut = new EmailVerificationService(
                loadMemberPort, saveMemberPort, tokenPort, mailPort
        );

        when(tokenPort.consumeForSignup("BAD_TOKEN"))
                .thenThrow(new IllegalArgumentException("nope"));

        // when / then
        assertThatThrownBy(() ->
                sut.confirmSignup("BAD_TOKEN")
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(400);
                    assertThat(rse.getReason()).contains("Invalid or expired token");
                });

        verify(tokenPort).consumeForSignup("BAD_TOKEN");
        verifyNoInteractions(loadMemberPort);
        verifyNoInteractions(saveMemberPort);
    }

    @Test
    @DisplayName("confirmSignup(): 토큰이 유효하나 member를 못 찾으면 400 BAD_REQUEST를 던진다")
    void confirmSignup_memberNotFound_throws400() {
        // given
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        EmailVerificationTokenPort tokenPort = mock(EmailVerificationTokenPort.class);
        MailPort mailPort = mock(MailPort.class);

        EmailVerificationService sut = new EmailVerificationService(
                loadMemberPort, saveMemberPort, tokenPort, mailPort
        );

        UUID memberUuid = UUID.randomUUID();
        when(tokenPort.consumeForSignup("GOOD_TOKEN"))
                .thenReturn(memberUuid);

        when(loadMemberPort.loadById(new MemberId(memberUuid)))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                sut.confirmSignup("GOOD_TOKEN")
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(400);
                    assertThat(rse.getReason()).contains("Invalid token");
                });

        verify(tokenPort).consumeForSignup("GOOD_TOKEN");
        verify(loadMemberPort).loadById(new MemberId(memberUuid));
        verifyNoInteractions(saveMemberPort);
    }

    @Test
    @DisplayName("confirmSignup(): 아직 미인증이면 markEmailVerified() 후 saveMemberPort.save()를 호출한다")
    void confirmSignup_notYetVerified_marksAndSaves() {
        // given
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        EmailVerificationTokenPort tokenPort = mock(EmailVerificationTokenPort.class);
        MailPort mailPort = mock(MailPort.class);

        EmailVerificationService sut = new EmailVerificationService(
                loadMemberPort, saveMemberPort, tokenPort, mailPort
        );

        UUID memberUuid = UUID.randomUUID();
        MemberId vo = new MemberId(memberUuid);

        Member mockMember = mock(Member.class);
        when(mockMember.emailVerified()).thenReturn(false);

        when(tokenPort.consumeForSignup("TOKEN123"))
                .thenReturn(memberUuid);

        when(loadMemberPort.loadById(vo))
                .thenReturn(Optional.of(mockMember));

        // when
        sut.confirmSignup("TOKEN123");

        // then
        verify(tokenPort).consumeForSignup("TOKEN123");
        verify(loadMemberPort).loadById(vo);

        // 아직 미인증이므로 markEmailVerified() 후 save 호출
        verify(mockMember).emailVerified();
        verify(mockMember).markEmailVerified();
        verify(saveMemberPort).save(mockMember);

        verifyNoInteractions(mailPort);
    }

    @Test
    @DisplayName("confirmSignup(): 이미 인증된 회원이면 save를 호출하지 않고 멱등적으로 끝난다")
    void confirmSignup_alreadyVerified_isIdempotent() {
        // given
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        EmailVerificationTokenPort tokenPort = mock(EmailVerificationTokenPort.class);
        MailPort mailPort = mock(MailPort.class);

        EmailVerificationService sut = new EmailVerificationService(
                loadMemberPort, saveMemberPort, tokenPort, mailPort
        );

        UUID memberUuid = UUID.randomUUID();
        MemberId vo = new MemberId(memberUuid);

        Member mockMember = mock(Member.class);
        when(mockMember.emailVerified()).thenReturn(true);

        when(tokenPort.consumeForSignup("TOKEN999"))
                .thenReturn(memberUuid);

        when(loadMemberPort.loadById(vo))
                .thenReturn(Optional.of(mockMember));

        // when
        sut.confirmSignup("TOKEN999");

        // then
        verify(tokenPort).consumeForSignup("TOKEN999");
        verify(loadMemberPort).loadById(vo);

        // 이미 인증된 상태이면 markEmailVerified() / save()는 안 불려야 한다
        verify(mockMember).emailVerified();
        verify(mockMember, never()).markEmailVerified();
        verify(saveMemberPort, never()).save(any());

        verifyNoInteractions(mailPort);
    }

    // --- requestForChange() -------------------------------------------------

    @Test
    @DisplayName("requestForChange(): 멤버가 없으면 아무 것도 하지 않는다")
    void requestForChange_memberNotFound_isSilent() {
        // given
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        EmailVerificationTokenPort tokenPort = mock(EmailVerificationTokenPort.class);
        MailPort mailPort = mock(MailPort.class);

        EmailVerificationService sut = new EmailVerificationService(
                loadMemberPort, saveMemberPort, tokenPort, mailPort
        );

        UUID memberUuid = UUID.randomUUID();
        when(loadMemberPort.loadById(new MemberId(memberUuid)))
                .thenReturn(Optional.empty());

        // when
        sut.requestForChange(memberUuid, "new@example.com");

        // then
        verify(loadMemberPort).loadById(new MemberId(memberUuid));
        verifyNoInteractions(saveMemberPort);
        verifyNoInteractions(tokenPort);
        verifyNoInteractions(mailPort);
    }

    @Test
    @DisplayName("requestForChange(): 멤버가 있으면 토큰 발급 후 새로운 이메일로 메일을 보낸다")
    void requestForChange_valid_sendsEmail() {
        // given
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        EmailVerificationTokenPort tokenPort = mock(EmailVerificationTokenPort.class);
        MailPort mailPort = mock(MailPort.class);

        EmailVerificationService sut = new EmailVerificationService(
                loadMemberPort, saveMemberPort, tokenPort, mailPort
        );

        UUID memberUuid = UUID.randomUUID();
        Member mockMember = mock(Member.class);

        when(loadMemberPort.loadById(new MemberId(memberUuid)))
                .thenReturn(Optional.of(mockMember));

        when(tokenPort.issueForChange(eq(memberUuid), eq("new@example.com"), any()))
                .thenReturn("CHANGE_TOKEN_123");

        // when
        sut.requestForChange(memberUuid, "new@example.com");

        // then
        verify(loadMemberPort).loadById(new MemberId(memberUuid));
        verify(tokenPort).issueForChange(eq(memberUuid), eq("new@example.com"), any());

        verify(mailPort).send(
                eq("new@example.com"),
                contains("이메일 변경"),
                contains("CHANGE_TOKEN_123")
        );

        // 변경 요청 단계에서는 아직 DB 변경이 없으므로 save 호출 X
        verifyNoInteractions(saveMemberPort);
    }

    /**
     * requestForChange() 안에서는 new Email(newEmail) 로 VO 검증을 한다고 했지.
     * Email VO가 invalid 형식에서 IllegalArgumentException 같은 걸 던진다면
     * 그 예외는 여기서 바로 propagate 될 거야.
     * 그 동작을 검증한다.
     */
    @Test
    @DisplayName("requestForChange(): 이메일 형식이 잘못되면 Email VO 생성 시 예외가 전파된다")
    void requestForChange_invalidEmail_throwsFromEmailVO() {
        // given
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        EmailVerificationTokenPort tokenPort = mock(EmailVerificationTokenPort.class);
        MailPort mailPort = mock(MailPort.class);

        EmailVerificationService sut = new EmailVerificationService(
                loadMemberPort, saveMemberPort, tokenPort, mailPort
        );

        UUID memberUuid = UUID.randomUUID();
        Member mockMember = mock(Member.class);
        when(loadMemberPort.loadById(new MemberId(memberUuid)))
                .thenReturn(Optional.of(mockMember));

        // when / then
        assertThatThrownBy(() ->
                sut.requestForChange(memberUuid, "not-an-email!!")
        )
                .isInstanceOf(RuntimeException.class); // 구체적으로 IllegalArgumentException 등, Email VO 규칙에 맞게 조정 가능

        // invalid이므로 tokenPort / mailPort는 호출되면 안됨
        verifyNoInteractions(tokenPort);
        verifyNoInteractions(mailPort);
        verifyNoInteractions(saveMemberPort);
    }

    // --- confirmChange() ----------------------------------------------------

    @Test
    @DisplayName("confirmChange(): invalid token이면 400 BAD_REQUEST를 던진다")
    void confirmChange_invalidToken_throws400() {
        // given
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        EmailVerificationTokenPort tokenPort = mock(EmailVerificationTokenPort.class);
        MailPort mailPort = mock(MailPort.class);

        EmailVerificationService sut = new EmailVerificationService(
                loadMemberPort, saveMemberPort, tokenPort, mailPort
        );

        when(tokenPort.consumeForChange("BAD_TOKEN"))
                .thenThrow(new IllegalArgumentException("expired or invalid"));

        // when / then
        assertThatThrownBy(() ->
                sut.confirmChange("BAD_TOKEN")
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(400);
                    assertThat(rse.getReason()).contains("Invalid or expired token");
                });

        verify(tokenPort).consumeForChange("BAD_TOKEN");
        verifyNoInteractions(loadMemberPort);
        verifyNoInteractions(saveMemberPort);
    }

    @Test
    @DisplayName("confirmChange(): 토큰 소비 후 멤버를 못 찾으면 400 BAD_REQUEST를 던진다")
    void confirmChange_memberNotFound_throws400() {
        // given
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        EmailVerificationTokenPort tokenPort = mock(EmailVerificationTokenPort.class);
        MailPort mailPort = mock(MailPort.class);

        EmailVerificationService sut = new EmailVerificationService(
                loadMemberPort, saveMemberPort, tokenPort, mailPort
        );

        UUID memberUuid = UUID.randomUUID();

        EmailVerificationTokenPort.EmailChangePayload payload =
                new EmailVerificationTokenPort.EmailChangePayload(memberUuid, "new@example.com");

        when(tokenPort.consumeForChange("GOOD_TOKEN"))
                .thenReturn(payload);

        when(loadMemberPort.loadById(new MemberId(memberUuid)))
                .thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() ->
                sut.confirmChange("GOOD_TOKEN")
        )
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode().value()).isEqualTo(400);
                    assertThat(rse.getReason()).contains("Invalid token");
                });

        verify(tokenPort).consumeForChange("GOOD_TOKEN");
        verify(loadMemberPort).loadById(new MemberId(memberUuid));
        verifyNoInteractions(saveMemberPort);
    }

    @Test
    @DisplayName("confirmChange(): 멤버가 존재하면 changeEmail() 후 saveMemberPort.save()를 호출한다")
    void confirmChange_success_updatesEmailAndSaves() {
        // given
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        EmailVerificationTokenPort tokenPort = mock(EmailVerificationTokenPort.class);
        MailPort mailPort = mock(MailPort.class);

        EmailVerificationService sut = new EmailVerificationService(
                loadMemberPort, saveMemberPort, tokenPort, mailPort
        );

        UUID memberUuid = UUID.randomUUID();

        EmailVerificationTokenPort.EmailChangePayload payload =
                new EmailVerificationTokenPort.EmailChangePayload(memberUuid, "new@example.com");

        Member mockMember = mock(Member.class);

        when(tokenPort.consumeForChange("CHANGE_TOKEN_1"))
                .thenReturn(payload);

        when(loadMemberPort.loadById(new MemberId(memberUuid)))
                .thenReturn(Optional.of(mockMember));

        // when
        sut.confirmChange("CHANGE_TOKEN_1");

        // then
        verify(tokenPort).consumeForChange("CHANGE_TOKEN_1");
        verify(loadMemberPort).loadById(new MemberId(memberUuid));

        verify(mockMember).changeEmail("new@example.com");
        verify(saveMemberPort).save(mockMember);

        verifyNoInteractions(mailPort);
    }
}
