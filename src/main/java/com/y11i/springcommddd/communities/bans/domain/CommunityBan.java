package com.y11i.springcommddd.communities.bans.domain;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "community_ban",
        uniqueConstraints = @UniqueConstraint(
                name="uk_community_ban_key",
                columnNames = {"community_ban_id"}
        )
)
@EntityListeners(AuditingEntityListener.class)
@Access(AccessType.FIELD)
public class CommunityBan implements AggregateRoot {
    @EmbeddedId
    private CommunityBanId banId;

    @Embedded
    private CommunityId communityId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "id", // ← MemberId 안의 필드명에 맞게 수정 (예: value, memberId 등)
                    column = @Column(name = "banned_member_id", columnDefinition = "BINARY(16)", nullable = false)
            )
    })
    private MemberId bannedMemberId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "id",
                    column = @Column(name = "processor_member_id", columnDefinition = "BINARY(16)", nullable = false)
            )
    })
    private MemberId processorId;

    @Embedded
    private BanReason reason;

    @CreatedDate
    @Column(name = "banned_at", nullable = false, updatable = false)
    private Instant bannedAt;

    @LastModifiedDate
    @Column(name="updated_at", nullable=false)
    private Instant updatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;         // null이면 영구 정지

    @Column(name="lifted_at")
    private Instant liftedAt;          // 해제 시각 (null이면 활성 중)

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(
                    name = "id", // MemberId 안의 필드명에 맞추기 (id / value / memberId 등)
                    column = @Column(
                            name = "lifted_by_member_id",
                            columnDefinition = "BINARY(16)",
                            nullable = true
                    )
            )
    })
    private MemberId liftedBy;         // 해제 실행자

    @Version
    private long version; // Optimistic Lock

    protected CommunityBan() {}

    /*─────────────────────────────────────────────*
     *  🔥 Factory Methods
     *─────────────────────────────────────────────*/

    /** 기간 정지 */
    public static CommunityBan temporaryBan(
            CommunityId communityId,
            MemberId target,
            MemberId processor,
            BanReason reason,
            Duration duration
    ) {
        if (duration == null || duration.isZero() || duration.isNegative())
            throw new IllegalArgumentException("Ban duration must be positive");

        CommunityBan ban = new CommunityBan();
        ban.banId = CommunityBanId.newId();
        ban.communityId = Objects.requireNonNull(communityId);
        ban.bannedMemberId = Objects.requireNonNull(target);
        ban.processorId = Objects.requireNonNull(processor);
        ban.reason = Objects.requireNonNull(reason);
        ban.bannedAt = Instant.now();
        ban.expiresAt = ban.bannedAt.plus(duration);
        return ban;
    }

    /** 영구 정지 */
    public static CommunityBan permanentBan(
            CommunityId communityId,
            MemberId target,
            MemberId processor,
            BanReason reason
    ) {
        CommunityBan ban = new CommunityBan();
        ban.banId = CommunityBanId.newId();
        ban.communityId = communityId;
        ban.bannedMemberId = target;
        ban.processorId = processor;
        ban.reason = reason;
        ban.bannedAt = Instant.now();
        ban.expiresAt = null;
        return ban;
    }


    /*─────────────────────────────────────────────*
     *  🔥 Domain Behaviors
     *─────────────────────────────────────────────*/

    /** 이 밴이 현재 유효한가? */
    public boolean isActive() {
        if (this.liftedAt != null) return false;      // 해제됨
        if (this.expiresAt == null) return true;       // 영구밴
        return Instant.now().isBefore(this.expiresAt); // 기간 내 활성
    }

    /** 밴 해제 */
    public void lift(MemberId actor) {
        if (!isActive()) return; // 이미 해제 or 만료: 멱등성
        this.liftedAt = Instant.now();
        this.liftedBy = actor;
    }

    /** 정지기간 연장 */
    public void extend(Duration extra) {
        if (expiresAt == null)
            throw new IllegalStateException("Permanent ban cannot be extended");

        this.expiresAt = this.expiresAt.plus(extra);
    }


    /*─────────────────────────────────────────────*
     *  Getters (읽기 전용)
     *─────────────────────────────────────────────*/

    public CommunityBanId banId() { return banId; }
    public CommunityId communityId() { return communityId; }
    public MemberId bannedMemberId() { return bannedMemberId; }
    public MemberId processorId() { return processorId; }
    public BanReason reason() { return reason; }
    public Instant bannedAt() { return bannedAt; }
    public Instant expiresAt() { return expiresAt; }
    public Instant liftedAt() { return liftedAt; }
    public MemberId liftedBy() { return liftedBy; }
    public boolean isPermanent() { return expiresAt == null; }
}
