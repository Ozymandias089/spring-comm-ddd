package com.y11i.springcommddd.communities.moderators.domain;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

/**
 * 커뮤니티별 모더레이터 역할 부여.
 * <p>한 멤버가 여러 커뮤니티의 모더레이터가 될 수 있습니다.</p>
 */
@Entity
@Table(
        name = "community_moderators",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_community_moderators_unique",
                columnNames = {"community_id", "member_id"}
        ),
        indexes = {
                @Index(name = "ix_community_moderators_member", columnList = "member_id"),
                @Index(name = "ix_community_moderators_community", columnList = "community_id")
        }
)
@EntityListeners(AuditingEntityListener.class)
@Access(AccessType.FIELD)
public class CommunityModerator implements AggregateRoot {

    @EmbeddedId
    private CommunityModeratorId id;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "community_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false))
    private CommunityId communityId;

    @Embedded
    @AttributeOverride(name = "id", column = @Column(name = "member_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false))
    private MemberId memberId;

    @CreatedDate
    @Column(name = "granted_at", nullable = false, updatable = false)
    private Instant grantedAt;

    protected CommunityModerator() {}

    protected CommunityModerator(CommunityId communityId, MemberId memberId) {
        this.id = CommunityModeratorId.newId();
        this.communityId = Objects.requireNonNull(communityId);
        this.memberId = Objects.requireNonNull(memberId);
    }

    public static CommunityModerator grant(CommunityId communityId, MemberId memberId) {
        return new CommunityModerator(communityId, memberId);
    }

    public CommunityModeratorId id() { return id; }
    public CommunityId communityId() { return communityId; }
    public MemberId memberId() { return memberId; }
    public Instant grantedAt() { return grantedAt; }
}
