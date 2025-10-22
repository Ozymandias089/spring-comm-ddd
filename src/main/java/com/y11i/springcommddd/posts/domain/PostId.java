package com.y11i.springcommddd.posts.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.util.Assert;

import java.util.UUID;

@Embeddable
public record PostId(
        @Column(name = "post_id", columnDefinition = "BINARY(16)", nullable = false)
        UUID id
) implements ValueObject {
    public PostId {
        Assert.notNull(id, "id must not be null");
    }

    public static PostId newId() {
        return new PostId(UUID.randomUUID());
    }
}
