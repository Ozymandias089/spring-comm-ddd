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
 * 커뮤니티별 모더레이터 역할 부여를 표현하는 애그리게잇 루트.
 *
 * <p><b>개요</b><br>
 * 하나의 {@link CommunityId}와 하나의 {@link MemberId}의 조합으로
 * “해당 커뮤니티의 모더레이터” 권한을 나타냅니다.
 * 동일 커뮤니티-회원 쌍은 유일해야 합니다.
 * </p>
 *
 * <p><b>테이블/제약</b></p>
 * <ul>
 *   <li>테이블: {@code community_moderators}</li>
 *   <li>유니크: {@code (community_id, member_id)}</li>
 *   <li>인덱스: {@code member_id}, {@code community_id}</li>
 * </ul>
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

    /**
     * 내부 생성자.
     *
     * @param communityId 커뮤니티 식별자
     * @param memberId    회원 식별자
     */
    protected CommunityModerator(CommunityId communityId, MemberId memberId) {
        this.id = CommunityModeratorId.newId();
        this.communityId = Objects.requireNonNull(communityId);
        this.memberId = Objects.requireNonNull(memberId);
    }

    /**
     * 팩토리 메서드: 모더레이터 권한을 부여합니다.
     *
     * @param communityId 커뮤니티 식별자
     * @param memberId    회원 식별자
     * @return 새 {@link CommunityModerator} 인스턴스
     */
    public static CommunityModerator grant(CommunityId communityId, MemberId memberId) {
        return new CommunityModerator(communityId, memberId);
    }

    // --- 접근자 ---
    public CommunityModeratorId id() { return id; }
    public CommunityId communityId() { return communityId; }
    public MemberId memberId() { return memberId; }
    public Instant grantedAt() { return grantedAt; }
}
