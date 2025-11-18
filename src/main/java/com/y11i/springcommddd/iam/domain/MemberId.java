package com.y11i.springcommddd.iam.domain;

import com.y11i.springcommddd.shared.domain.exception.InvalidIdentifierFormat;
import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * 회원 식별자 값 객체.
 *
 * <p><b>특징</b></p>
 * <ul>
 *   <li>UUID 기반 임베디드 값 객체</li>
 *   <li>DB 컬럼 타입: {@code BINARY(16)}</li>
 *   <li>불변(Immutable)</li>
 * </ul>
 */
@Embeddable
public record MemberId(
        @Column(name = "member_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
        UUID id
) implements ValueObject, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 생성자 유효성 검증: {@code id}는 null일 수 없습니다.
     *
     * @param id UUID 값
     * @throws NullPointerException id가 null인 경우
     */
    public MemberId {
        Objects.requireNonNull(id, "userId is required");
    }

    /** 무작위 UUID로 새로운 식별자를 생성합니다. */
    public static MemberId newId() {
        return new MemberId(UUID.randomUUID());
    }

    public static MemberId objectify(String id) {
        try {
            return new MemberId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            throw new InvalidIdentifierFormat("Invalid memberId: " + id);
        }
    }

    public String stringify() {
        return id.toString();
    }
}
