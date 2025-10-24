package com.y11i.springcommddd.posts.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * 게시글 식별자 값 객체.
 *
 * <p><b>특징</b></p>
 * <ul>
 *   <li>UUID 기반 임베디드 값 객체</li>
 *   <li>DB 컬럼: {@code BINARY(16)}</li>
 *   <li>불변(Immutable)</li>
 * </ul>
 */
@Embeddable
public record PostId(
        @Column(name = "post_id", columnDefinition = "BINARY(16)", nullable = false)
        UUID id
) implements ValueObject {

    /** 생성자 유효성 검증 */
    public PostId { Assert.notNull(id, "id must not be null"); }

    /** 무작위 UUID로 새 식별자를 생성합니다. */
    public static PostId newId() {
        return new PostId(UUID.randomUUID());
    }
}
