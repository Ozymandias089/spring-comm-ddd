package com.y11i.springcommddd.unit.iam;

import com.y11i.springcommddd.iam.api.support.MemberMapper;
import com.y11i.springcommddd.iam.application.port.in.RegisterMemberUseCase;
import com.y11i.springcommddd.iam.application.port.out.LoadMemberPort;
import com.y11i.springcommddd.iam.application.port.out.SaveMemberPort;
import com.y11i.springcommddd.iam.application.service.MemberService;
import com.y11i.springcommddd.iam.domain.Email;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.dto.MemberDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * MemberService 단위 테스트.
 *
 * 검증 범위:
 * - register():
 *    - PasswordEncoder.encode()가 호출되는가?
 *    - Member.register(...)로 생성된 Member가 saveMemberPort.save(...)로 넘겨지는가?
 *    - save()의 반환값이 MemberMapper.toMemberDTO()를 통해 최종 MemberDTO로 반환되는가?
 *
 * - findByEmail(), findById():
 *    - loadMemberPort에서 Optional<Member>를 가져오고,
 *      그걸 MemberMapper.toMemberDTO()로 매핑해 Optional<MemberDTO>로 돌려주는지.
 *    - 못 찾으면 Optional.empty()로 끝나는지.
 */
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Test
    @DisplayName("register() - 비밀번호는 인코딩되고 저장된 Member가 DTO로 반환된다")
    void register_encodesPassword_savesMember_andReturnsDTO() {
        // given
        LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
        SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        MemberService sut = new MemberService(loadMemberPort, saveMemberPort, passwordEncoder);

        // 입력 커맨드 (RegisterMemberUseCase.Command record 라고 가정)
        RegisterMemberUseCase.Command command = new RegisterMemberUseCase.Command(
                "user@example.com",
                "Tester",
                "raw-password-123"
        );

        // passwordEncoder.encode 호출 결과
        when(passwordEncoder.encode("raw-password-123"))
                .thenReturn("ENCODED_HASH");

        // Member.register(...)와 MemberMapper.toMemberDTO(...)는 static이라서
        // mockStatic으로 제어하자.
        Member mockMemberBeforeSave = mock(Member.class);
        Member mockMemberAfterSave = mock(Member.class); // save 이후 DB가 부여한 ID 등 반영된 상태라고 가정
        MemberDTO mappedDto = mock(MemberDTO.class);

        try (MockedStatic<Member> memberStatic = mockStatic(Member.class);
             MockedStatic<MemberMapper> mapperStatic = mockStatic(MemberMapper.class)) {

            // Member.register(email, displayName, encodedPassword) -> 새 Member
            memberStatic.when(() ->
                    Member.register("user@example.com", "Tester", "ENCODED_HASH")
            ).thenReturn(mockMemberBeforeSave);

            // saveMemberPort.save(...) -> 영속화된 Member 리턴
            when(saveMemberPort.save(mockMemberBeforeSave))
                    .thenReturn(mockMemberAfterSave);

            // MemberMapper.toMemberDTO(...) -> MemberDTO
            mapperStatic.when(() ->
                    MemberMapper.toMemberDTO(mockMemberAfterSave)
            ).thenReturn(mappedDto);

            // when
            MemberDTO result = sut.register(command);

            // then
            // 1) 비밀번호 인코딩이 rawPassword로 호출되었는가
            verify(passwordEncoder).encode("raw-password-123");

            // 2) saveMemberPort.save()에는 Member.register()의 결과가 넘어갔는가
            verify(saveMemberPort).save(mockMemberBeforeSave);

            // 3) 최종 반환은 mapper의 결과여야 한다
            assertThat(result).isSameAs(mappedDto);

            // 4) Member.register()가 정확한 인자(email, displayName, encodedPw)로 호출되었는지도 검증
            memberStatic.verify(() ->
                    Member.register("user@example.com", "Tester", "ENCODED_HASH")
            );
        }
    }

    @Nested
    class FindTests {

        @Test
        @DisplayName("findByEmail() - 존재하는 이메일이면 Optional<MemberDTO>를 반환한다")
        void findByEmail_found_returnsMappedDto() {
            // given
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            MemberService sut = new MemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            String emailStr = "user@example.com";

            Member domainMember = mock(Member.class);
            MemberDTO dto = mock(MemberDTO.class);

            // lenient stubbing: 어떤 Email이 오든 Optional.of(domainMember) 리턴하라고 해둔다
            lenient().when(loadMemberPort.loadByEmail(any(Email.class)))
                    .thenReturn(Optional.of(domainMember));

            try (MockedStatic<MemberMapper> mapperStatic = mockStatic(MemberMapper.class)) {

                mapperStatic.when(() ->
                        MemberMapper.toMemberDTO(domainMember)
                ).thenReturn(dto);

                // when
                Optional<MemberDTO> result = sut.findByEmail(emailStr);

                // then
                assertThat(result).isPresent();
                assertThat(result.get()).isSameAs(dto);

                // 실제로 loadByEmail(...)이 한 번은 호출됐는지 확인
                verify(loadMemberPort, times(1)).loadByEmail(any(Email.class));
            }
        }

        @Test
        @DisplayName("findByEmail() - 존재하지 않으면 Optional.empty()를 반환한다")
        void findByEmail_notFound_returnsEmpty() {
            // given
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            MemberService sut = new MemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            String emailStr = "ghost@example.com";

            // lenient stubbing: 어떤 Email이 와도 Optional.empty() 리턴
            lenient().when(loadMemberPort.loadByEmail(any(Email.class)))
                    .thenReturn(Optional.empty());

            // when
            Optional<MemberDTO> result = sut.findByEmail(emailStr);

            // then
            assertThat(result).isEmpty();

            verify(loadMemberPort, times(1)).loadByEmail(any(Email.class));
        }

        @Test
        @DisplayName("findById() - 존재하는 ID면 Optional<MemberDTO>를 반환한다")
        void findById_found_returnsMappedDto() {
            // given
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            MemberService sut = new MemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID id = UUID.randomUUID();

            Member domainMember = mock(Member.class);
            MemberDTO dto = mock(MemberDTO.class);

            // 같은 문제 방지: MemberId는 VO이므로 any(MemberId.class) 매칭 + lenient stubbing
            lenient().when(loadMemberPort.loadById(any(MemberId.class)))
                    .thenReturn(Optional.of(domainMember));

            try (MockedStatic<MemberMapper> mapperStatic = mockStatic(MemberMapper.class)) {

                mapperStatic.when(() ->
                        MemberMapper.toMemberDTO(domainMember)
                ).thenReturn(dto);

                // when
                Optional<MemberDTO> result = sut.findById(id);

                // then
                assertThat(result).isPresent();
                assertThat(result.get()).isSameAs(dto);

                verify(loadMemberPort, times(1)).loadById(any(MemberId.class));
            }
        }

        @Test
        @DisplayName("findById() - 존재하지 않으면 Optional.empty()를 반환한다")
        void findById_notFound_returnsEmpty() {
            // given
            LoadMemberPort loadMemberPort = mock(LoadMemberPort.class);
            SaveMemberPort saveMemberPort = mock(SaveMemberPort.class);
            PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

            MemberService sut = new MemberService(loadMemberPort, saveMemberPort, passwordEncoder);

            UUID id = UUID.randomUUID();

            // lenient stubbing으로 Optional.empty()
            lenient().when(loadMemberPort.loadById(any(MemberId.class)))
                    .thenReturn(Optional.empty());

            // when
            Optional<MemberDTO> result = sut.findById(id);

            // then
            assertThat(result).isEmpty();

            verify(loadMemberPort, times(1)).loadById(any(MemberId.class));
        }
    }
}
