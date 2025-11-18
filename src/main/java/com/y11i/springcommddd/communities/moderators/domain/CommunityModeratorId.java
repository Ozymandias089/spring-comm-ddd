package com.y11i.springcommddd.communities.moderators.domain;

import com.y11i.springcommddd.shared.domain.exception.InvalidIdentifierFormat;
import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

/**
 * {@link CommunityModerator}의 식별자 값 객체.
 * <p>UUID 기반이며 DB에는 {@code BINARY(16)}으로 저장됩니다.</p>
 */
@Embeddable
public record CommunityModeratorId(
        @Column(name = "community_moderator_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
        UUID id
) implements ValueObject {

    /** 생성자 유효성 검증. */
    public CommunityModeratorId { Objects.requireNonNull(id); }

    /**
     * 무작위 UUID로 새 식별자를 생성합니다.
     *
     * @return 새로운 {@code CommunityModeratorId}
     */
    public static  CommunityModeratorId newId() { return new CommunityModeratorId(UUID.randomUUID()); }

    public static CommunityModeratorId objectify(String id) {
        try {
            return new CommunityModeratorId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            throw new InvalidIdentifierFormat("Invalid community moderator id: " + id);
        }
    }

    public String stringify() {
        return id.toString();
    }
}
