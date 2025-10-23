package com.y11i.springcommddd.votes.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public record PostVoteId(
        @Column(name="post_vote_id", columnDefinition="BINARY(16)", nullable=false, updatable=false)
        UUID id
) implements ValueObject {
    public PostVoteId { Objects.requireNonNull(id); }
    public static PostVoteId newId(){ return new PostVoteId(UUID.randomUUID()); }
}
