package com.y11i.springcommddd.comments.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

/**
 * 댓글 식별자 값 객체.
 *
 * <p><b>특징</b></p>
 * <ul>
 *   <li>UUID 기반의 임베디드 값 객체</li>
 *   <li>DB 컬럼 타입: {@code BINARY(16)}</li>
 *   <li>불변(Immutable)</li>
 * </ul>
 */
@Embeddable
public record CommentId(
        @Column(name = "comment_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
        UUID id
) implements ValueObject {

    /**
     * 생성자 유효성 검증: {@code id}는 null일 수 없습니다.
     *
     * @param id UUID 값
     * @throws NullPointerException id가 null인 경우
     */
    public CommentId { Objects.requireNonNull(id, "comment id cannot be null"); }

    /**
     * 무작위 UUID로 새로운 댓글 식별자를 생성합니다.
     *
     * @return 새 {@link CommentId}
     */
    public static CommentId newId() { return new CommentId(UUID.randomUUID()); }

}
