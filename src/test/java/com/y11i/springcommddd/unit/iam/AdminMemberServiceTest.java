package com.y11i.springcommddd.unit.iam;

import com.y11i.springcommddd.iam.application.port.in.AdminMemberUseCase;
import com.y11i.springcommddd.iam.application.port.out.LoadMemberPort;
import com.y11i.springcommddd.iam.application.port.out.SaveMemberPort;
import com.y11i.springcommddd.iam.application.service.AdminMemberService;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.MemberRole;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AdminMemberService 단위 테스트.
 * <p>
 * 검증 포인트:
 * <p>
 * grantAdmin():
 *  - 대상 멤버를 loadById()로 불러온다.
 *  - member.grantRole(MemberRole.ADMIN) 호출
 *  - saveMemberPort.save(member) 호출
 *  - 멤버 없으면 orElseThrow()로 예외(일반적으로 NoSuchElementException)
 * <p>
 * revokeAdmin():
 *  - 대상 멤버를 loadById()로 불러온다.
 *  - SecurityContextHolder에서 현재 인증 주체를 확인
 *    - 만약 현재 principal name(= authentication.getName())과 target member의 email().value()가 같으면
 *      IllegalStateException("cannot revoke your own ADMIN role")
 *  - 그렇지 않으면 member.revokeRole(MemberRole.ADMIN) 후 save()
 * <p>
 * setStatus():
 *  - 대상 멤버 load
 *  - cmd.status()에 따라
 *      "ACTIVE"   -> member.activate()
 *      "SUSPENDED"-> member.suspend()
 *      "DELETED"  -> if 자기 자신이면 IllegalStateException("cannot delete your own account")
 *                     else member.markDeleted()
 *      나머지     -> IllegalArgumentException("unknown status: ...")
 *  - saveMemberPort.save(member)
 * <p>
 * createAdminAccount():
 *  - passwordEncoder.encode(rawPassword)
 *  - Member.register(email, displayName, encodedPw) [static]
 *  - member.grantRole(MemberRole.ADMIN)
 *  - saveMemberPort.save(member)
 *  - member.memberId().id() 를 반환
 */
@ExtendWith(MockitoExtension.class)
class AdminMemberServiceTest {

    @Nested
    class GrantAdminTests {

        @Test
        @DisplayName("grantAdmin(): 대상 멤버에게 ADMIN 역할을 부여하고 저장한다")
        void grantAdmin_grantsRoleAndSaves() {
            // given
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            AdminMemberService sut = new AdminMemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID targetId = UUID.randomUUID();
            AdminMemberUseCase.GrantAdminCommand cmd =
                    new AdminMemberUseCase.GrantAdminCommand(targetId);

            Member targetMember = mock(Member.class);

            when(loadMemberPort.loadById(new MemberId(targetId)))
                    .thenReturn(Optional.of(targetMember));

            // when
            sut.grantAdmin(cmd);

            // then
            verify(loadMemberPort).loadById(new MemberId(targetId));
            verify(targetMember).grantRole(MemberRole.ADMIN);
            verify(saveMemberPort).save(targetMember);
        }

        @Test
        @DisplayName("grantAdmin(): 멤버가 존재하지 않으면 NoSuchElementException이 발생한다")
        void grantAdmin_memberNotFound_throws() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            AdminMemberService sut = new AdminMemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID targetId = UUID.randomUUID();
            AdminMemberUseCase.GrantAdminCommand cmd =
                    new AdminMemberUseCase.GrantAdminCommand(targetId);

            when(loadMemberPort.loadById(new MemberId(targetId)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.grantAdmin(cmd))
                    .isInstanceOf(NoSuchElementException.class);

            verify(loadMemberPort).loadById(new MemberId(targetId));
            verifyNoInteractions(saveMemberPort);
        }
    }

    @Nested
    class RevokeAdminTests {

        @Test
        @DisplayName("revokeAdmin(): 자기 자신이면 ADMIN 권한을 회수하려 할 때 IllegalStateException을 던진다")
        void revokeAdmin_cannotRevokeSelf() {
            // given
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            AdminMemberService sut = new AdminMemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID targetId = UUID.randomUUID();
            AdminMemberUseCase.RevokeAdminCommand cmd =
                    new AdminMemberUseCase.RevokeAdminCommand(targetId);

            Member targetMember = mock(Member.class);

            // 인증 컨텍스트에 "admin@example.com" 라는 principal로 로그인했다고 가정
            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken("admin@example.com", "pw", "ROLE_ADMIN")
            );

            when(loadMemberPort.loadById(new MemberId(targetId)))
                    .thenReturn(Optional.of(targetMember));

            // targetMember.email().value() 가 현재 principal 과 같다고 가정
            var emailVo = mock(com.y11i.springcommddd.iam.domain.Email.class);
            when(emailVo.value()).thenReturn("admin@example.com");
            when(targetMember.email()).thenReturn(emailVo);

            // when / then
            assertThatThrownBy(() -> sut.revokeAdmin(cmd))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cannot revoke your own ADMIN role");

            verify(loadMemberPort).loadById(new MemberId(targetId));
            // 자기 자신에 대한 revoke 이므로 revokeRole/save는 호출되면 안 된다
            verify(targetMember, never()).revokeRole(MemberRole.ADMIN);
            verifyNoInteractions(saveMemberPort);

            // SecurityContext cleanup은 @AfterEach에서 할 수도 있지만 여기선 수동 정리
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("revokeAdmin(): 타인이라면 ADMIN 권한을 회수하고 저장한다")
        void revokeAdmin_revokesRoleAndSaves_forOtherUser() {
            // given
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            AdminMemberService sut = new AdminMemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID targetId = UUID.randomUUID();
            AdminMemberUseCase.RevokeAdminCommand cmd =
                    new AdminMemberUseCase.RevokeAdminCommand(targetId);

            Member targetMember = mock(Member.class);

            // 인증 주체는 다른 이메일
            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken("root-admin@example.com", "pw", "ROLE_ADMIN")
            );

            when(loadMemberPort.loadById(new MemberId(targetId)))
                    .thenReturn(Optional.of(targetMember));

            // 대상 멤버 이메일은 다른 값
            var emailVo = mock(com.y11i.springcommddd.iam.domain.Email.class);
            when(emailVo.value()).thenReturn("somebody-else@example.com");
            when(targetMember.email()).thenReturn(emailVo);

            // when
            sut.revokeAdmin(cmd);

            // then
            verify(loadMemberPort).loadById(new MemberId(targetId));
            verify(targetMember).revokeRole(MemberRole.ADMIN);
            verify(saveMemberPort).save(targetMember);

            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("revokeAdmin(): 멤버가 존재하지 않으면 NoSuchElementException")
        void revokeAdmin_memberNotFound_throws() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            AdminMemberService sut = new AdminMemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID targetId = UUID.randomUUID();
            AdminMemberUseCase.RevokeAdminCommand cmd =
                    new AdminMemberUseCase.RevokeAdminCommand(targetId);

            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken("admin@example.com", "pw", "ROLE_ADMIN")
            );

            when(loadMemberPort.loadById(new MemberId(targetId)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.revokeAdmin(cmd))
                    .isInstanceOf(NoSuchElementException.class);

            verify(loadMemberPort).loadById(new MemberId(targetId));
            verifyNoInteractions(saveMemberPort);

            SecurityContextHolder.clearContext();
        }
    }

    @Nested
    class SetStatusTests {

        @Test
        @DisplayName("setStatus(): ACTIVE -> member.activate() 후 save()")
        void setStatus_active_callsActivateAndSaves() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            AdminMemberService sut = new AdminMemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID targetId = UUID.randomUUID();
            AdminMemberUseCase.SetStatusCommand cmd =
                    new AdminMemberUseCase.SetStatusCommand(targetId, "ACTIVE");

            Member targetMember = mock(Member.class);

            when(loadMemberPort.loadById(new MemberId(targetId)))
                    .thenReturn(Optional.of(targetMember));

            sut.setStatus(cmd);

            verify(loadMemberPort).loadById(new MemberId(targetId));
            verify(targetMember).activate();
            verify(saveMemberPort).save(targetMember);
        }

        @Test
        @DisplayName("setStatus(): SUSPENDED -> member.suspend() 후 save()")
        void setStatus_suspended_callsSuspendAndSaves() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            AdminMemberService sut = new AdminMemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID targetId = UUID.randomUUID();
            AdminMemberUseCase.SetStatusCommand cmd =
                    new AdminMemberUseCase.SetStatusCommand(targetId, "SUSPENDED");

            Member targetMember = mock(Member.class);

            when(loadMemberPort.loadById(new MemberId(targetId)))
                    .thenReturn(Optional.of(targetMember));

            sut.setStatus(cmd);

            verify(loadMemberPort).loadById(new MemberId(targetId));
            verify(targetMember).suspend();
            verify(saveMemberPort).save(targetMember);
        }

        @Test
        @DisplayName("setStatus(): DELETED 인데 자기 자신이면 IllegalStateException")
        void setStatus_deletedButSelf_throws() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            AdminMemberService sut = new AdminMemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID targetId = UUID.randomUUID();
            AdminMemberUseCase.SetStatusCommand cmd =
                    new AdminMemberUseCase.SetStatusCommand(targetId, "DELETED");

            Member targetMember = mock(Member.class);

            // 현재 인증된 사용자 이메일과 대상 멤버 이메일이 같게 설정
            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken("victim@example.com", "pw", "ROLE_ADMIN")
            );

            var emailVo = mock(com.y11i.springcommddd.iam.domain.Email.class);
            when(emailVo.value()).thenReturn("victim@example.com");
            when(targetMember.email()).thenReturn(emailVo);

            when(loadMemberPort.loadById(new MemberId(targetId)))
                    .thenReturn(Optional.of(targetMember));

            assertThatThrownBy(() -> sut.setStatus(cmd))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cannot delete your own account");

            verify(loadMemberPort).loadById(new MemberId(targetId));
            verify(targetMember, never()).markDeleted();
            verifyNoInteractions(saveMemberPort);

            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("setStatus(): DELETED 인데 타인인 경우 markDeleted() 후 save()")
        void setStatus_deletedOtherUser_marksDeletedAndSaves() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            AdminMemberService sut = new AdminMemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID targetId = UUID.randomUUID();
            AdminMemberUseCase.SetStatusCommand cmd =
                    new AdminMemberUseCase.SetStatusCommand(targetId, "DELETED");

            Member targetMember = mock(Member.class);

            // 현재 인증된 사용자는 별도 이메일
            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken("root-admin@example.com", "pw", "ROLE_ADMIN")
            );

            var emailVo = mock(com.y11i.springcommddd.iam.domain.Email.class);
            when(emailVo.value()).thenReturn("other-user@example.com");
            when(targetMember.email()).thenReturn(emailVo);

            when(loadMemberPort.loadById(new MemberId(targetId)))
                    .thenReturn(Optional.of(targetMember));

            sut.setStatus(cmd);

            verify(loadMemberPort).loadById(new MemberId(targetId));
            verify(targetMember).markDeleted();
            verify(saveMemberPort).save(targetMember);

            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("setStatus(): 알 수 없는 status면 IllegalArgumentException")
        void setStatus_unknownStatus_throws() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            AdminMemberService sut = new AdminMemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID targetId = UUID.randomUUID();
            AdminMemberUseCase.SetStatusCommand cmd =
                    new AdminMemberUseCase.SetStatusCommand(targetId, "BANANA");

            Member targetMember = mock(Member.class);

            when(loadMemberPort.loadById(new MemberId(targetId)))
                    .thenReturn(Optional.of(targetMember));

            assertThatThrownBy(() -> sut.setStatus(cmd))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("unknown status: BANANA");

            verify(loadMemberPort).loadById(new MemberId(targetId));
            verifyNoInteractions(saveMemberPort);
        }

        @Test
        @DisplayName("setStatus(): 멤버를 찾을 수 없으면 NoSuchElementException")
        void setStatus_memberNotFound_throws() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            AdminMemberService sut = new AdminMemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID targetId = UUID.randomUUID();
            AdminMemberUseCase.SetStatusCommand cmd =
                    new AdminMemberUseCase.SetStatusCommand(targetId, "ACTIVE");

            when(loadMemberPort.loadById(new MemberId(targetId)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.setStatus(cmd))
                    .isInstanceOf(NoSuchElementException.class);

            verify(loadMemberPort).loadById(new MemberId(targetId));
            verifyNoInteractions(saveMemberPort);
        }
    }

    @Nested
    class CreateAdminAccountTests {

        @Test
        @DisplayName("createAdminAccount(): 새 관리자 계정을 생성하고 ADMIN 역할을 부여한 뒤 저장하고 ID를 반환한다")
        void createAdminAccount_createsAdminWithEncodedPassword() {
            // given
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            AdminMemberService sut = new AdminMemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            AdminMemberUseCase.CreateAdminCommand cmd =
                    new AdminMemberUseCase.CreateAdminCommand(
                            "new-admin@example.com",
                            "Site Admin",
                            "plain-secret"
                    );

            when(passwordEncoder.encode("plain-secret"))
                    .thenReturn("ENCODED_SECRET");

            Member newMemberBeforeSave = mock(Member.class);
            Member newMemberAfterSave  = mock(Member.class);

            UUID generatedId = UUID.randomUUID();
            MemberId memberIdVO = new MemberId(generatedId);

            when(newMemberAfterSave.memberId()).thenReturn(memberIdVO);

            // Member.register(...) 는 static 이므로 mockStatic 사용
            try (MockedStatic<com.y11i.springcommddd.iam.domain.Member> memberStatic =
                         mockStatic(com.y11i.springcommddd.iam.domain.Member.class)) {

                memberStatic.when(() ->
                        com.y11i.springcommddd.iam.domain.Member.register(
                                "new-admin@example.com",
                                "Site Admin",
                                "ENCODED_SECRET"
                        )
                ).thenReturn(newMemberBeforeSave);

                when(saveMemberPort.save(newMemberBeforeSave))
                        .thenReturn(newMemberAfterSave);

                // when
                UUID result = sut.createAdminAccount(cmd);

                // then
                // 1. 비밀번호 인코딩 확인
                verify(passwordEncoder).encode("plain-secret");

                // 2. Member.register(...) 호출 확인
                memberStatic.verify(() ->
                        com.y11i.springcommddd.iam.domain.Member.register(
                                "new-admin@example.com",
                                "Site Admin",
                                "ENCODED_SECRET"
                        )
                );

                // 3. ADMIN 역할 부여
                verify(newMemberBeforeSave).grantRole(MemberRole.ADMIN);

                // 4. 저장 호출
                verify(saveMemberPort).save(newMemberBeforeSave);

                // 5. 반환값은 save 이후 member.memberId().id()
                assertThat(result).isEqualTo(generatedId);
            }
        }
    }
}
