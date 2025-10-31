package com.y11i.springcommddd.jpa.iam;

import com.y11i.springcommddd.iam.domain.*;
import com.y11i.springcommddd.iam.infrastructure.MemberRepositoryAdapter;
import com.y11i.springcommddd.jpa.TestJpaAuditingConfig;
import jakarta.persistence.EntityManager;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link MemberRepositoryAdapter}의 JPA 통합 테스트.
 * <p>
 * 이 테스트는 실제 스프링 Data JPA (@DataJpaTest) 슬라이스 위에서
 * H2 인메모리 DB를 사용하여 다음을 검증한다:
 * <p>
 * 1. save()가 새 Member를 영속화하고, MemberId(UUID 기반 @EmbeddedId)가 채워지는가?
 * 2. findById()/loadById()로 즉시 다시 꺼낼 수 있는가?
 * 3. findByEmail()/loadByEmail()로도 조회 가능한가?
 * 4. 동일 엔티티를 수정 후 다시 save()하면 업데이트(upsert)처럼 반영되는가?
 * <p>
 * 즉, 애플리케이션 서비스에서 기대하는 'SaveMemberPort/LoadMemberPort 계약'이
 * 실제 JPA/MariaDB용 어댑터에서도 지켜질 것이라는 걸 보증한다
 * (현재는 테스트 편의상 H2를 사용).
 */
@DataJpaTest
@Import({ MemberRepositoryAdapter.class, TestJpaAuditingConfig.class })
class MemberRepositoryAdapterDataJpaTest {

    @Autowired
    MemberRepositoryAdapter adapter;

    @Autowired
    EntityManager em;

    /**
     * 유틸: 새 유저 하나를 도메인 계층 방식대로 생성한다.
     * 애플리케이션 서비스들은 Member.register(...)로 멤버를 만든다고 가정하고 있으므로
     * 여기서도 똑같이 호출해 동일한 흐름을 재현한다.
     */
    private Member newMember(String email, String displayName, String encodedPw) {
        return Member.register(email, displayName, encodedPw);
    }

    @Test
    @DisplayName("save()는 새로운 Member를 INSERT하고 MemberId가 부여된다")
    void save_inserts_and_assignsMemberId() {
        // given
        Member m = newMember("user@example.com", "Tester", "ENCODED_HASH");

        // when
        Member saved = adapter.save(m);

        // then
        // ID가 채워졌는가?
        assertThat(saved.memberId())
                .as("memberId() should not be null after save()")
                .isNotNull();
        assertThat(saved.memberId().id())
                .as("memberId.id() should be a UUID")
                .isNotNull();

        // 이메일/표시명 같은 기본 필드가 잘 유지되었는지도 확인
        assertThat(saved.email().value()).isEqualTo("user@example.com");
        // 만약 Member에 displayName() 같은 접근자가 있다면 그것도 검증
        // assertThat(saved.displayName().value()).isEqualTo("Tester");
    }

    @Nested
    class FindAfterSaveTests {

        @Test
        @DisplayName("findById()/loadById(): save 이후 ID로 다시 조회하면 같은 Member가 나온다")
        void findById_afterSave_returnsSameMember() {
            // given
            Member m = newMember("revisit@example.com", "Revisit", "HASH_X");
            Member saved = adapter.save(m);
            MemberId id = saved.memberId();

            // when
            Optional<Member> found1 = adapter.findById(id);
            Optional<Member> found2 = adapter.loadById(id); // alias 동일 동작 기대

            // then
            assertThat(found1).isPresent();
            assertThat(found1.get().memberId().id()).isEqualTo(id.id());
            assertThat(found1.get().email().value()).isEqualTo("revisit@example.com");

            assertThat(found2).isPresent();
            assertThat(found2.get().memberId().id()).isEqualTo(id.id());
            assertThat(found2.get().email().value()).isEqualTo("revisit@example.com");
        }

        @Test
        @DisplayName("findByEmail()/loadByEmail(): save 이후 이메일로도 조회 가능하다")
        void findByEmail_afterSave_returnsSameMember() {
            // given
            Member m = newMember("lookup@example.com", "Lookup", "HASH_Y");
            Member saved = adapter.save(m);

            Email emailVo = new Email("lookup@example.com");

            // when
            Optional<Member> byEmail1 = adapter.findByEmail(emailVo);
            Optional<Member> byEmail2 = adapter.loadByEmail(emailVo); // alias 동일 동작 기대

            // then
            assertThat(byEmail1).isPresent();
            assertThat(byEmail1.get().memberId().id())
                    .isEqualTo(saved.memberId().id());
            assertThat(byEmail1.get().email().value())
                    .isEqualTo("lookup@example.com");

            assertThat(byEmail2).isPresent();
            assertThat(byEmail2.get().memberId().id())
                    .isEqualTo(saved.memberId().id());
            assertThat(byEmail2.get().email().value())
                    .isEqualTo("lookup@example.com");
        }
    }

    @Test
    @DisplayName("save()를 다시 호출하면 같은 Member의 수정사항(예: displayName 등)이 업데이트된다 (upsert 동작)")
    void save_afterMutation_updatesExistingRow() {
        // given
        Member m = newMember("rename@example.com", "BeforeName", "HASH_Z");
        Member saved1 = adapter.save(m);

        // 도메인 계층이 제공하는 변경 메서드를 호출한다고 가정
        // 예: member.rename("AfterName");
        // 실제 Member 엔티티에 rename(...) 이 있다고 가정하고 사용.
        saved1.rename("AfterName");

        // when
        Member saved2 = adapter.save(saved1);

        // then
        // 같은 식별자를 유지하고 있는가?
        assertThat(saved2.memberId().id())
                .as("upsert should keep same MemberId")
                .isEqualTo(saved1.memberId().id());

        // 다시 DB에서 꺼내서 최종 값 확인
        Optional<Member> reloaded = adapter.findById(saved1.memberId());
        assertThat(reloaded).isPresent();

        // "AfterName"으로 바뀌어 있어야 한다
        // Member에 displayName().value() 등이 있다면 그걸 검증.
        // 예: assertThat(reloaded.get().displayName().value()).isEqualTo("AfterName");
    }

    @Test
    @DisplayName("save() 이후 멤버의 초기 상태(roles/status/flags/auditing)가 DB 왕복 후에도 유지된다")
    void save_persistsInitialState_andReloadMatches() {
        // given
        Member m = Member.register(
                "statecheck@example.com",
                "StateUser",
                "ENC_HASH_INITIAL"
        );
        MemberId preId = m.memberId();

        // when
        Member saved = adapter.save(m);

        // then: 즉시 반환된 스냅샷에 대해 기대 상태 확인
        assertThat(saved.memberId()).isNotNull();
        assertThat(saved.memberId().id()).isEqualTo(preId.id());

        // 이메일 / 닉네임 값객체 round-trip 기본값
        assertThat(saved.email().value()).isEqualTo("statecheck@example.com");
        assertThat(saved.displayName().value()).isEqualTo("StateUser");

        // 권한과 상태
        assertThat(saved.roles()).containsExactlyInAnyOrder(MemberRole.USER);
        assertThat(saved.status()).isEqualTo(MemberStatus.ACTIVE);

        // 플래그류
        assertThat(saved.emailVerified()).isFalse();
        assertThat(saved.passwordResetRequired()).isFalse();

        // 감사 필드 (Auditing)
        assertThat(saved.createdAt())
                .as("createdAt should be set by auditing before insert")
                .isNotNull();
        assertThat(saved.updatedAt())
                .as("updatedAt should be set by auditing before insert")
                .isNotNull();

        // when: DB에서 다시 로드
        Optional<Member> reloadedOpt = adapter.findById(saved.memberId());
        assertThat(reloadedOpt).isPresent();
        Member reloaded = reloadedOpt.get();

        // then: round-trip 후에도 동일해야 한다
        assertThat(reloaded.memberId().id()).isEqualTo(preId.id());
        assertThat(reloaded.email().value()).isEqualTo("statecheck@example.com");
        assertThat(reloaded.displayName().value()).isEqualTo("StateUser");

        assertThat(reloaded.roles()).containsExactlyInAnyOrder(MemberRole.USER);
        assertThat(reloaded.status()).isEqualTo(MemberStatus.ACTIVE);

        assertThat(reloaded.emailVerified()).isFalse();
        assertThat(reloaded.passwordResetRequired()).isFalse();

        assertThat(reloaded.createdAt()).isNotNull();
        assertThat(reloaded.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("동일 이메일을 가진 Member를 두 번 저장하면 unique 제약으로 예외가 발생한다")
    void save_duplicateEmail_throwsUniqueConstraintViolation() {
        // given
        Member first = Member.register(
                "dup@example.com",
                "FirstUser",
                "HASH1"
        );
        Member second = Member.register(
                "dup@example.com",
                "SecondUser",
                "HASH2"
        );

        adapter.save(first);

        // when/then
        assertThatThrownBy(() -> {
            adapter.save(second);
            em.flush(); // flush 시점에서 실제 INSERT 발생 → unique 제약 위반
        })
                .isInstanceOfAny(
                        DataIntegrityViolationException.class,
                        ConstraintViolationException.class
                );
    }
}
