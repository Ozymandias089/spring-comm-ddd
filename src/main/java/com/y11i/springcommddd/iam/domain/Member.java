package com.y11i.springcommddd.iam.domain;

import com.y11i.springcommddd.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

/**
 * 회원(Member) 애그리게잇 루트.
 *
 * <p><b>개요</b><br>
 * 로그인 자격(이메일/비밀번호 해시), 표시명, 상태(활성/정지/삭제)를 관리하는 도메인 모델입니다.
 * 비밀번호 변경 이력(업데이트 시각)과 “비밀번호 재설정 필요” 플래그를 포함합니다.
 * </p>
 *
 * <p><b>영속성/테이블</b></p>
 * <ul>
 *   <li>테이블: {@code members}</li>
 *   <li>유니크 제약: {@code uk_members_email} ({@code email})</li>
 *   <li>감사 필드: {@link #createdAt}, {@link #updatedAt}</li>
 *   <li>낙관적 락 버전: {@link #version}</li>
 * </ul>
 *
 * <p><b>불변식/규칙</b></p>
 * <ul>
 *   <li>{@link Email}, {@link DisplayName}, {@link PasswordHash} 값 객체의 검증 준수</li>
 *   <li>삭제( {@link MemberStatus#DELETED} ) 상태에서는 변경 동작 금지</li>
 *   <li>비밀번호는 항상 해시(인코딩)된 값만 저장</li>
 * </ul>
 */
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

    // -----------------------------------------------------
    // 식별자/상태/감사 필드
    // -----------------------------------------------------

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

    // -----------------------------------------------------
    // 인증 관련 값
    // -----------------------------------------------------

    /** 해시된(인코딩된) 비밀번호 */
    @Embedded
    private PasswordHash passwordHash;

    /** 마지막 비밀번호 변경 시각 */
    @Column(name = "password_updated_at", nullable = false)
    private Instant passwordUpdatedAt = Instant.EPOCH; // 최초 등록 시 갱신

    /** 보안 이슈 등으로 비밀번호 재설정이 필요한 경우 true */
    @Column(name = "password_reset_required", nullable = false)
    private boolean passwordResetRequired = false;

    /** JPA 기본 생성자. 외부에서 직접 호출하지 않습니다. */
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

    // -----------------------------------------------------
    // 생성 섹션 (정적 팩토리)
    // -----------------------------------------------------

    /**
     * 신규 회원을 등록합니다.
     *
     * @param email           로그인에 사용할 이메일(문자열)
     * @param displayName     표시명(문자열)
     * @param encodedPassword 이미 해시(인코딩)된 비밀번호
     * @return 새 {@link Member} 인스턴스
     * @throws IllegalArgumentException 유효하지 않은 이메일/표시명/비밀번호 해시
     */
    public static Member register(String email, String displayName, String encodedPassword) {
        return new Member(new Email(email), new DisplayName(displayName), PasswordHash.fromEncoded(encodedPassword));
    }

    // -----------------------------------------------------
    // 도메인 동작 섹션 (변경/상태 전이)
    // -----------------------------------------------------

    /**
     * 표시명을 변경합니다.
     *
     * <p><b>전제조건</b>: 삭제 상태가 아니어야 합니다.</p>
     * <p><b>부작용</b>: {@link #displayName}이 갱신됩니다.</p>
     *
     * @param newDisplayName 새 표시명
     * @throws IllegalStateException 삭제된 회원에 대한 변경 시도
     * @throws IllegalArgumentException 새 표시명이 규칙을 위반
     */
    public void rename(String newDisplayName) {
        ensureNotDeleted("deleted member cannot rename");
        this.displayName = new DisplayName(newDisplayName);
    }

    /**
     * 새 비밀번호 해시를 설정합니다.
     *
     * <p><b>전제조건</b>: 삭제 상태가 아니어야 합니다.</p>
     * <p><b>부작용</b>: 해시와 변경 시각이 갱신되며, 재설정 플래그가 해제됩니다.</p>
     *
     * @param encodedPassword 이미 해시(인코딩)된 문자열
     * @throws IllegalStateException 삭제된 회원에 대한 변경 시도
     * @throws IllegalArgumentException 해시 값이 비어있음
     */

    public void setNewPassword(String encodedPassword) {
        ensureNotDeleted("deleted member cannot change password");
        this.passwordHash = PasswordHash.fromEncoded(encodedPassword);
        this.passwordUpdatedAt = Instant.now();
        this.passwordResetRequired = false;
    }

    /**
     * 이메일을 변경합니다.
     *
     * <p><b>전제조건</b>: 삭제 상태가 아니어야 합니다.</p>
     * <p><b>부작용</b>: {@link #email}이 갱신됩니다.</p>
     *
     * @param newEmail 새 이메일
     * @throws IllegalStateException 삭제된 회원에 대한 변경 시도
     * @throws IllegalArgumentException 이메일 형식 위반
     */
    public void changeEmail(String newEmail) {
        ensureNotDeleted("deleted member cannot change email");
        this.email = new Email(newEmail);
    }

    /**
     * 회원을 일시 정지 상태로 전환합니다.
     *
     * @throws IllegalStateException 삭제된 회원은 정지할 수 없음
     */
    public void suspend() {
        if (status == MemberStatus.DELETED) throw new IllegalStateException("deleted member cannot be suspended");
        this.status = MemberStatus.SUSPENDED;
    }

    /**
     * 회원을 활성 상태로 전환합니다.
     *
     * @throws IllegalStateException 삭제된 회원은 활성화할 수 없음
     */
    public void activate() {
        if (status == MemberStatus.DELETED) throw new IllegalStateException("deleted member cannot be activated");
        this.status = MemberStatus.ACTIVE;
    }

    /** 회원을 삭제 상태로 표시합니다(소프트 삭제). */
    public void markDeleted() {
        this.status = MemberStatus.DELETED;
    }

    /**
     * 비밀번호 재설정이 필요함을 표시합니다.
     *
     * @throws IllegalStateException 삭제된 회원에 대한 변경 시도
     */
    public void requirePasswordReset() {
        ensureNotDeleted("deleted member cannot be forced to reset password");
        this.passwordResetRequired = true;
    }

    // -----------------------------------------------------
    // 내부 검증 섹션
    // -----------------------------------------------------

    /**
     * 삭제 상태가 아님을 보장합니다.
     *
     * @param msg 예외 메시지
     * @throws IllegalStateException 삭제 상태인 경우
     */
    private void ensureNotDeleted(String msg) {
        if (status == MemberStatus.DELETED) throw new IllegalStateException(msg);
    }

    // -----------------------------------------------------
    // 접근자 섹션 (읽기 전용)
    // -----------------------------------------------------

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
