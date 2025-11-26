package com.y11i.springcommddd.communities.bans.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import com.y11i.springcommddd.shared.domain.exception.InvalidIdentifierFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public record CommunityBanId(
        @Column(name = "community_ban_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
        UUID id
) implements ValueObject {
    public CommunityBanId { Objects.requireNonNull(id); }

    public static CommunityBanId newId() { return new CommunityBanId(UUID.randomUUID()); }

    public static CommunityBanId objectify(String id) {
        try {
            return new CommunityBanId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            throw new InvalidIdentifierFormat("Invalid community ban id: " + id);
        }
    }

    public String stringify() {
        return id.toString();
    }
}
