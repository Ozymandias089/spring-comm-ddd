package com.y11i.springcommddd.iam.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

@Embeddable
public record MemberId(
        @Column(name = "member_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
        UUID id
) implements ValueObject {
    public MemberId {
        Objects.requireNonNull(id, "userId is required");
    }

    public static MemberId newId() {
        return new MemberId(UUID.randomUUID());
    }
}
