package com.y11i.springcommddd.votes.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public record CommentVoteId(
        @Column(name="comment_vote_id", columnDefinition="BINARY(16)", nullable=false, updatable=false)
        UUID id
) implements ValueObject {
    public CommentVoteId { Objects.requireNonNull(id); }
    public static CommentVoteId newId() { return new CommentVoteId(UUID.randomUUID()); }
}
