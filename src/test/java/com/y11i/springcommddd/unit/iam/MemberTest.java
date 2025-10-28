package com.y11i.springcommddd.unit.iam;

import com.y11i.springcommddd.iam.domain.*;
import com.y11i.springcommddd.iam.domain.exception.MemberDeletedModificationNotAllowed;
import com.y11i.springcommddd.iam.domain.exception.MemberStatusTransitionNotAllowed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class MemberTest {

    // --- Happy: 신규 등록 기본 상태/필드 확인 ---
    @Test
    @DisplayName("register: ACTIVE 상태, USER 롤 기본 부여, 이메일 소문자/트림, 비번시각 설정, reset 플래그 false")
    void register_ok() {
        var m = Member.register("  USER@Email.Com  ", "  Alice  ", "$bcrypt$xxx");

        assertSoftly(soft -> {
            soft.assertThat(m.status()).isEqualTo(MemberStatus.ACTIVE);
            soft.assertThat(m.roles()).containsExactly(MemberRole.USER);
            soft.assertThat(m.email().value()).isEqualTo("user@email.com");
            soft.assertThat(m.displayName().value()).isEqualTo("Alice");
            soft.assertThat(m.passwordHash().encoded()).isEqualTo("$bcrypt$xxx");
            soft.assertThat(m.passwordUpdatedAt()).isNotNull();
            soft.assertThat(m.passwordResetRequired()).isFalse();
        });
    }

    // --- Happy: 표시명/이메일 변경 ---
    @Test
    @DisplayName("rename/changeEmail: 삭제 상태가 아니면 정상 변경")
    void rename_changeEmail_ok_whenNotDeleted() {
        var m = Member.register("a@b.com", "Bob", "$h1");
        m.rename("Bobby");
        m.changeEmail("  B@C.Com ");

        assertThat(m.displayName().value()).isEqualTo("Bobby");
        assertThat(m.email().value()).isEqualTo("b@c.com");
    }

    // --- Guard: 삭제 상태에서는 모든 변경 금지 ---
    @Test
    @DisplayName("markDeleted 이후 rename/changeEmail/setNewPassword/grantRole/revokeRole/requirePasswordReset 모두 금지")
    void all_modifications_forbidden_whenDeleted() {
        var m = Member.register("a@b.com", "Bob", "$h1");
        m.grantRole(MemberRole.ADMIN);
        m.markDeleted();

        assertThatThrownBy(() -> m.rename("X"))
                .isInstanceOf(MemberDeletedModificationNotAllowed.class);
        assertThatThrownBy(() -> m.changeEmail("x@y.com"))
                .isInstanceOf(MemberDeletedModificationNotAllowed.class);
        assertThatThrownBy(() -> m.setNewPassword("$h2"))
                .isInstanceOf(MemberDeletedModificationNotAllowed.class);
        assertThatThrownBy(() -> m.grantRole(MemberRole.ADMIN))
                .isInstanceOf(MemberDeletedModificationNotAllowed.class);
        assertThatThrownBy(() -> m.revokeRole(MemberRole.ADMIN))
                .isInstanceOf(MemberDeletedModificationNotAllowed.class);
        assertThatThrownBy(m::requirePasswordReset)
                .isInstanceOf(MemberDeletedModificationNotAllowed.class);
    }

    // --- 상태 전이: 삭제 상태에서는 suspend/activate 금지 ---
    @Test
    @DisplayName("DELETED 상태에서는 suspend/activate 불가")
    void deleted_cannot_suspend_or_activate() {
        var m = Member.register("a@b.com", "Bob", "$h1");
        m.markDeleted();

        assertThatThrownBy(m::suspend)
                .isInstanceOf(MemberStatusTransitionNotAllowed.class)
                .hasMessageContaining("suspended");

        assertThatThrownBy(m::activate)
                .isInstanceOf(MemberStatusTransitionNotAllowed.class)
                .hasMessageContaining("activated");
    }

    // --- 상태 전이: ACTIVE → SUSPENDED → ACTIVE ---
    @Test
    @DisplayName("ACTIVE → suspend() → SUSPENDED → activate() → ACTIVE")
    void suspend_then_activate_ok() {
        var m = Member.register("a@b.com", "Bob", "$h1");
        m.suspend();
        assertThat(m.status()).isEqualTo(MemberStatus.SUSPENDED);

        m.activate();
        assertThat(m.status()).isEqualTo(MemberStatus.ACTIVE);
    }

    // --- 비밀번호 변경 ---
    @Test
    @DisplayName("setNewPassword: 해시 교체 & passwordUpdatedAt 갱신 & reset 플래그 해제")
    void setNewPassword_updates_fields() {
        var m = Member.register("a@b.com", "Bob", "$h1");
        var before = m.passwordUpdatedAt();

        m.requirePasswordReset();
        m.setNewPassword("$h2");

        assertSoftly(soft -> {
            soft.assertThat(m.passwordHash().encoded()).isEqualTo("$h2");
            soft.assertThat(m.passwordResetRequired()).isFalse();
            soft.assertThat(m.passwordUpdatedAt()).isNotNull();
            soft.assertThat(m.passwordUpdatedAt()).isNotEqualTo(before);
        });
    }

    // --- 역할 부여/회수 ---
    @Test
    @DisplayName("grant/revoke 역할: USER는 회수 no-op, ADMIN은 추가/제거 가능")
    void grant_revoke_roles() {
        var m = Member.register("a@b.com", "Bob", "$h1");
        m.grantRole(MemberRole.ADMIN);
        assertThat(m.roles()).contains(MemberRole.USER, MemberRole.ADMIN);

        m.revokeRole(MemberRole.USER); // no-op
        assertThat(m.roles()).contains(MemberRole.USER, MemberRole.ADMIN);

        m.revokeRole(MemberRole.ADMIN);
        assertThat(m.roles()).containsExactly(MemberRole.USER);
    }
}
