package com.y11i.springcommddd.posts.media.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

/** PostAsset 식별자 값 객체(UUID, BINARY(16)) */
@Embeddable
public record PostAssetId(
        @Column(name = "post_asset_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
        UUID id
) implements ValueObject {
    public PostAssetId { Objects.requireNonNull(id); }
    public static PostAssetId newId() {return new PostAssetId(UUID.randomUUID());}
}
