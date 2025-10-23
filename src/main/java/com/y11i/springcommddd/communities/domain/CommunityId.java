package com.y11i.springcommddd.communities.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public record CommunityId(
        @Column(name="community_id", columnDefinition="BINARY(16)", nullable=false, updatable=false)
        UUID id
) implements ValueObject {
    public CommunityId { Objects.requireNonNull(id); }
    public static CommunityId newId() {return new CommunityId(UUID.randomUUID());}
}
