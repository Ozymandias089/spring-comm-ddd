package com.y11i.springcommddd.comments.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public record CommentId(
        @Column(name = "comment_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
        UUID id
) implements ValueObject {

    public CommentId { Objects.requireNonNull(id, "comment id cannot be null"); }

    public static CommentId newId() { return new CommentId(UUID.randomUUID()); }

}
