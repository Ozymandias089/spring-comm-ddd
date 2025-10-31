package com.y11i.springcommddd.unit.iam;

import com.y11i.springcommddd.iam.api.support.MemberMapper;
import com.y11i.springcommddd.iam.application.port.in.ManageProfileUseCase;
import com.y11i.springcommddd.iam.application.port.out.LoadMemberPort;
import com.y11i.springcommddd.iam.application.port.out.SaveMemberPort;
import com.y11i.springcommddd.iam.application.service.MyPageService;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.PasswordHash;
import com.y11i.springcommddd.iam.dto.MemberDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * MyPageService 단위 테스트.
 * <p>
 * 검증 범위:
 *  - rename():
 *      - loadMemberPort.loadById()로 멤버 로드
 *      - Member.rename() 호출
 *      - saveMemberPort.save() 호출
 *      - MemberMapper.toMemberDTO() 반환
 *  - changeEmail():
 *      - Member.changeEmail() 동일 패턴
 *  - changePassword():
 *      - passwordEncoder.matches()로 현재 비번 확인
 *      - 틀리면 BadCredentialsException
 *      - 맞으면 passwordEncoder.encode()로 새 비밀번호 해싱 후
 *        member.setNewPassword() → save → DTO 매핑
 *  - changeProfileImage(), changeBannerImage():
 *      - 해당 도메인 메서드 호출 + save + DTO 매핑
 */
@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    /**
     * passwordHash()가 반환하는 객체를 우리가 직접 간단히 흉내 내는 작은 helper.
     * 실제 도메인 타입이 뭔지 몰라도 encoded()만 있으면 된다.
     */
    static class FakePasswordHash {
        private final String encoded;
        FakePasswordHash(String encoded) { this.encoded = encoded; }
        public String encoded() { return encoded; }
    }

    @Nested
    class RenameTests {

        @Test
        @DisplayName("rename(): 멤버를 로드해서 rename() 호출 후 save()하고 DTO로 반환한다")
        void rename_happyPath() {
            // given
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            MyPageService sut = new MyPageService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID memberUuid = UUID.randomUUID();
            ManageProfileUseCase.RenameCommand cmd =
                    new ManageProfileUseCase.RenameCommand(memberUuid, "NewNick");

            Member loadedMember = mock(Member.class);
            Member savedMember = mock(Member.class);
            MemberDTO mappedDto = mock(MemberDTO.class);

            when(loadMemberPort.loadById(new MemberId(memberUuid)))
                    .thenReturn(Optional.of(loadedMember));
            when(saveMemberPort.save(loadedMember))
                    .thenReturn(savedMember);

            try (MockedStatic<MemberMapper> mapperStatic = mockStatic(MemberMapper.class)) {
                mapperStatic.when(() -> MemberMapper.toMemberDTO(savedMember))
                        .thenReturn(mappedDto);

                // when
                MemberDTO result = sut.rename(cmd);

                // then
                verify(loadMemberPort).loadById(new MemberId(memberUuid));
                verify(loadedMember).rename("NewNick");
                verify(saveMemberPort).save(loadedMember);

                mapperStatic.verify(() -> MemberMapper.toMemberDTO(savedMember));
                assertThat(result).isSameAs(mappedDto);
            }
        }

        @Test
        @DisplayName("rename(): 멤버를 찾지 못하면 NoSuchElementException이 발생한다")
        void rename_memberNotFound_throws() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            MyPageService sut = new MyPageService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID memberUuid = UUID.randomUUID();
            ManageProfileUseCase.RenameCommand cmd =
                    new ManageProfileUseCase.RenameCommand(memberUuid, "NewNick");

            when(loadMemberPort.loadById(new MemberId(memberUuid)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.rename(cmd))
                    .isInstanceOf(NoSuchElementException.class);

            verify(loadMemberPort).loadById(new MemberId(memberUuid));
            verifyNoInteractions(saveMemberPort);
        }
    }

    @Nested
    class ChangeEmailTests {

        @Test
        @DisplayName("changeEmail(): 멤버를 로드해서 changeEmail() 후 save()하고 DTO 반환")
        void changeEmail_happyPath() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            MyPageService sut = new MyPageService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID memberUuid = UUID.randomUUID();
            ManageProfileUseCase.ChangeEmailCommand cmd =
                    new ManageProfileUseCase.ChangeEmailCommand(memberUuid, "new@example.com");

            Member loadedMember = mock(Member.class);
            Member savedMember = mock(Member.class);
            MemberDTO mappedDto = mock(MemberDTO.class);

            when(loadMemberPort.loadById(new MemberId(memberUuid)))
                    .thenReturn(Optional.of(loadedMember));
            when(saveMemberPort.save(loadedMember))
                    .thenReturn(savedMember);

            try (MockedStatic<MemberMapper> mapperStatic = mockStatic(MemberMapper.class)) {
                mapperStatic.when(() -> MemberMapper.toMemberDTO(savedMember))
                        .thenReturn(mappedDto);

                MemberDTO result = sut.changeEmail(cmd);

                verify(loadedMember).changeEmail("new@example.com");
                verify(saveMemberPort).save(loadedMember);

                mapperStatic.verify(() -> MemberMapper.toMemberDTO(savedMember));
                assertThat(result).isSameAs(mappedDto);
            }
        }

        @Test
        @DisplayName("changeEmail(): 멤버를 찾지 못하면 NoSuchElementException 발생")
        void changeEmail_memberNotFound_throws() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            MyPageService sut = new MyPageService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID memberUuid = UUID.randomUUID();
            ManageProfileUseCase.ChangeEmailCommand cmd =
                    new ManageProfileUseCase.ChangeEmailCommand(memberUuid, "new@example.com");

            when(loadMemberPort.loadById(new MemberId(memberUuid)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.changeEmail(cmd))
                    .isInstanceOf(NoSuchElementException.class);

            verify(loadMemberPort).loadById(new MemberId(memberUuid));
            verifyNoInteractions(saveMemberPort);
        }
    }

    @Nested
    class ChangePasswordTests {

        @Test
        @DisplayName("changePassword(): 현재 비밀번호가 틀리면 BadCredentialsException 발생")
        void changePassword_wrongCurrent_throwsBadCredentials() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            MyPageService sut = new MyPageService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID memberUuid = UUID.randomUUID();
            ManageProfileUseCase.ChangePasswordCommand cmd =
                    new ManageProfileUseCase.ChangePasswordCommand(memberUuid, "NEW_PW", "OLD_WRONG");

            Member loadedMember = mock(Member.class);

            PasswordHash hash = PasswordHash.fromEncoded("ENCODED_OLD");
            when(loadedMember.passwordHash()).thenReturn(hash);

            when(loadMemberPort.loadById(new MemberId(memberUuid)))
                    .thenReturn(Optional.of(loadedMember));

            // passwordEncoder.matches(currentPassword, storedHash) -> false
            when(passwordEncoder.matches("OLD_WRONG", "ENCODED_OLD"))
                    .thenReturn(false);

            assertThatThrownBy(() -> sut.changePassword(cmd))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessageContaining("current password does not match");

            verify(loadMemberPort).loadById(new MemberId(memberUuid));
            verify(passwordEncoder).matches("OLD_WRONG", "ENCODED_OLD");

            // 비번 틀리면 save 호출 안 해야 함
            verifyNoInteractions(saveMemberPort);
        }

        @Test
        @DisplayName("changePassword(): 현재 비밀번호가 맞으면 새 비밀번호 인코딩 후 저장하고 DTO 반환")
        void changePassword_success_encodesAndSaves() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            MyPageService sut = new MyPageService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID memberUuid = UUID.randomUUID();
            ManageProfileUseCase.ChangePasswordCommand cmd =
                    new ManageProfileUseCase.ChangePasswordCommand(memberUuid, "NEW_PW", "OLD_OK");

            Member loadedMember = mock(Member.class);
            Member savedMember = mock(Member.class);
            MemberDTO mappedDto = mock(MemberDTO.class);

            PasswordHash hash = PasswordHash.fromEncoded("ENCODED_OLD");
            when(loadedMember.passwordHash()).thenReturn(hash);

            when(loadMemberPort.loadById(new MemberId(memberUuid)))
                    .thenReturn(Optional.of(loadedMember));

            // 현재 비밀번호 일치
            when(passwordEncoder.matches("OLD_OK", "ENCODED_OLD"))
                    .thenReturn(true);

            // 새 비밀번호 인코딩
            when(passwordEncoder.encode("NEW_PW"))
                    .thenReturn("ENCODED_NEW");

            when(saveMemberPort.save(loadedMember))
                    .thenReturn(savedMember);

            try (MockedStatic<MemberMapper> mapperStatic = mockStatic(MemberMapper.class)) {
                mapperStatic.when(() -> MemberMapper.toMemberDTO(savedMember))
                        .thenReturn(mappedDto);

                // when
                MemberDTO result = sut.changePassword(cmd);

                // then
                verify(passwordEncoder).matches("OLD_OK", "ENCODED_OLD");
                verify(passwordEncoder).encode("NEW_PW");
                verify(loadedMember).setNewPassword("ENCODED_NEW");
                verify(saveMemberPort).save(loadedMember);

                mapperStatic.verify(() -> MemberMapper.toMemberDTO(savedMember));
                assertThat(result).isSameAs(mappedDto);
            }
        }

        @Test
        @DisplayName("changePassword(): 멤버를 찾지 못하면 NoSuchElementException 발생")
        void changePassword_memberNotFound_throws() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            MyPageService sut = new MyPageService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID memberUuid = UUID.randomUUID();
            ManageProfileUseCase.ChangePasswordCommand cmd =
                    new ManageProfileUseCase.ChangePasswordCommand(memberUuid, "NEW_PW", "CURR");

            when(loadMemberPort.loadById(new MemberId(memberUuid)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.changePassword(cmd))
                    .isInstanceOf(NoSuchElementException.class);

            verify(loadMemberPort).loadById(new MemberId(memberUuid));
            verifyNoInteractions(passwordEncoder);
            verifyNoInteractions(saveMemberPort);
        }
    }

    @Nested
    class ChangeProfileImageTests {

        @Test
        @DisplayName("changeProfileImage(): 이미지 URL 변경 후 save()하고 DTO 반환")
        void changeProfileImage_happyPath() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            MyPageService sut = new MyPageService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID memberUuid = UUID.randomUUID();
            ManageProfileUseCase.ChangeProfileImageCommand cmd =
                    new ManageProfileUseCase.ChangeProfileImageCommand(memberUuid, "https://cdn/img.png");

            Member loadedMember = mock(Member.class);
            Member savedMember = mock(Member.class);
            MemberDTO mappedDto = mock(MemberDTO.class);

            when(loadMemberPort.loadById(new MemberId(memberUuid)))
                    .thenReturn(Optional.of(loadedMember));
            when(saveMemberPort.save(loadedMember))
                    .thenReturn(savedMember);

            try (MockedStatic<MemberMapper> mapperStatic = mockStatic(MemberMapper.class)) {
                mapperStatic.when(() -> MemberMapper.toMemberDTO(savedMember))
                        .thenReturn(mappedDto);

                MemberDTO result = sut.changeProfileImage(cmd);

                verify(loadedMember).changeProfileImage("https://cdn/img.png");
                verify(saveMemberPort).save(loadedMember);

                mapperStatic.verify(() -> MemberMapper.toMemberDTO(savedMember));
                assertThat(result).isSameAs(mappedDto);
            }
        }

        @Test
        @DisplayName("changeProfileImage(): 멤버를 찾지 못하면 NoSuchElementException 발생")
        void changeProfileImage_memberNotFound_throws() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            MyPageService sut = new MyPageService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID memberUuid = UUID.randomUUID();
            ManageProfileUseCase.ChangeProfileImageCommand cmd =
                    new ManageProfileUseCase.ChangeProfileImageCommand(memberUuid, "https://cdn/img.png");

            when(loadMemberPort.loadById(new MemberId(memberUuid)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.changeProfileImage(cmd))
                    .isInstanceOf(NoSuchElementException.class);

            verify(loadMemberPort).loadById(new MemberId(memberUuid));
            verifyNoInteractions(saveMemberPort);
        }
    }

    @Nested
    class ChangeBannerImageTests {

        @Test
        @DisplayName("changeBannerImage(): 배너 URL 변경 후 save()하고 DTO 반환")
        void changeBannerImage_happyPath() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            MyPageService sut = new MyPageService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID memberUuid = UUID.randomUUID();
            ManageProfileUseCase.ChangeBannerImageCommand cmd =
                    new ManageProfileUseCase.ChangeBannerImageCommand(memberUuid, "https://cdn/banner.png");

            Member loadedMember = mock(Member.class);
            Member savedMember = mock(Member.class);
            MemberDTO mappedDto = mock(MemberDTO.class);

            when(loadMemberPort.loadById(new MemberId(memberUuid)))
                    .thenReturn(Optional.of(loadedMember));
            when(saveMemberPort.save(loadedMember))
                    .thenReturn(savedMember);

            try (MockedStatic<MemberMapper> mapperStatic = mockStatic(MemberMapper.class)) {
                mapperStatic.when(() -> MemberMapper.toMemberDTO(savedMember))
                        .thenReturn(mappedDto);

                MemberDTO result = sut.changeBannerImage(cmd);

                verify(loadedMember).changeBannerImage("https://cdn/banner.png");
                verify(saveMemberPort).save(loadedMember);

                mapperStatic.verify(() -> MemberMapper.toMemberDTO(savedMember));
                assertThat(result).isSameAs(mappedDto);
            }
        }

        @Test
        @DisplayName("changeBannerImage(): 멤버를 찾지 못하면 NoSuchElementException 발생")
        void changeBannerImage_memberNotFound_throws() {
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            MyPageService sut = new MyPageService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID memberUuid = UUID.randomUUID();
            ManageProfileUseCase.ChangeBannerImageCommand cmd =
                    new ManageProfileUseCase.ChangeBannerImageCommand(memberUuid, "https://cdn/banner.png");

            when(loadMemberPort.loadById(new MemberId(memberUuid)))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.changeBannerImage(cmd))
                    .isInstanceOf(NoSuchElementException.class);

            verify(loadMemberPort).loadById(new MemberId(memberUuid));
            verifyNoInteractions(saveMemberPort);
        }
    }
}
