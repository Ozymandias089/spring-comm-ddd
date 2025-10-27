package com.y11i.springcommddd.communities.moderators.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public record CommunityModeratorId(
        @Column(name = "community_moderator_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
        UUID id
) implements ValueObject {
    public CommunityModeratorId { Objects.requireNonNull(id); }
    public static  CommunityModeratorId newId() { return new CommunityModeratorId(UUID.randomUUID()); }
}
