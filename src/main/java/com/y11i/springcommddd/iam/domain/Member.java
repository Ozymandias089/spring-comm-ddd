package com.y11i.springcommddd.iam.domain;

import com.y11i.springcommddd.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "members",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_members_email", columnNames = {"email"})
        }
)
@EntityListeners(AuditingEntityListener.class)
@Access(AccessType.FIELD)
public class Member implements AggregateRoot {

    @EmbeddedId
    private MemberId memberId;

    @Embedded
    private Email email;

    @Embedded
    private DisplayName displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    // --- Password ---
    @Embedded
    private PasswordHash passwordHash;

    @Column(name = "password_updated_at", nullable = false)
    private Instant passwordUpdatedAt = Instant.EPOCH; // 최초 등록 시 갱신

    // (선택) 비밀번호 리셋이 요구되는 상태
    @Column(name = "password_reset_required", nullable = false)
    private boolean passwordResetRequired = false;

    protected Member() {}

    private Member(Email email, DisplayName displayName, PasswordHash passwordHash) {
        this.memberId = MemberId.newId();
        this.email = Objects.requireNonNull(email);
        this.displayName = Objects.requireNonNull(displayName);
        this.status = MemberStatus.ACTIVE;
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
        this.passwordUpdatedAt = Instant.now();     // 비번 최초 설정 시각 기록
        this.passwordResetRequired = false;
    }

    public static Member register(String email, String displayName, String encodedPassword) {
        return new Member(new Email(email), new DisplayName(displayName), PasswordHash.fromEncoded(encodedPassword));
    }

    // ===== 도메인 동작 =====
    public void rename(String newDisplayName) {
        ensureNotDeleted("deleted member cannot rename");
        this.displayName = new DisplayName(newDisplayName);
    }

    /** 새 비밀번호 설정(이미 인코딩된 문자열을 주입) */
    public void setNewPassword(String encodedPassword) {
        ensureNotDeleted("deleted member cannot change password");
        this.passwordHash = PasswordHash.fromEncoded(encodedPassword);
        this.passwordUpdatedAt = Instant.now();
        this.passwordResetRequired = false;
    }

    public void changeEmail(String newEmail) {
        ensureNotDeleted("deleted member cannot change email");
        this.email = new Email(newEmail);
    }

    public void suspend() {
        if (status == MemberStatus.DELETED) throw new IllegalStateException("deleted member cannot be suspended");
        this.status = MemberStatus.SUSPENDED;
    }

    public void activate() {
        if (status == MemberStatus.DELETED) throw new IllegalStateException("deleted member cannot be activated");
        this.status = MemberStatus.ACTIVE;
    }

    public void markDeleted() {
        this.status = MemberStatus.DELETED;
    }

    /** 비밀번호 리셋 요구 플래그 (침해 징후 등) */
    public void requirePasswordReset() {
        ensureNotDeleted("deleted member cannot be forced to reset password");
        this.passwordResetRequired = true;
    }

    private void ensureNotDeleted(String msg) {
        if (status == MemberStatus.DELETED) throw new IllegalStateException(msg);
    }

    /** Accessors (Read-only) */
    public MemberId memberId() { return memberId; }
    public Email email() { return email; }
    public DisplayName displayName() { return displayName; }
    public MemberStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public long version() { return version; }
    public PasswordHash passwordHash() { return passwordHash; }
    public Instant passwordUpdatedAt() { return passwordUpdatedAt; }
    public boolean passwordResetRequired() { return passwordResetRequired; }
}
