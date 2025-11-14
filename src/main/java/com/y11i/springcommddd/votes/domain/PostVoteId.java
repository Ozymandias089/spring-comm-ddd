package com.y11i.springcommddd.votes.domain;

import com.y11i.springcommddd.common.exception.InvalidIdentifierFormatException;
import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

/**
 * 게시글 투표 식별자 값 객체.
 *
 * <p><b>특징</b></p>
 * <ul>
 *   <li>UUID 기반, 임베디드 값 객체</li>
 *   <li>DB 컬럼: {@code BINARY(16)}</li>
 *   <li>불변(Immutable)</li>
 * </ul>
 */
@Embeddable
public record PostVoteId(
        @Column(name="post_vote_id", columnDefinition="BINARY(16)", nullable=false, updatable=false)
        UUID id
) implements ValueObject {

    /** 생성자 유효성 검증 */
    public PostVoteId { Objects.requireNonNull(id); }

    /** 무작위 UUID로 새 식별자를 생성합니다. */
    public static PostVoteId newId(){ return new PostVoteId(UUID.randomUUID()); }

    public static PostVoteId objectify(String id){
        try {
            return new PostVoteId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            throw new InvalidIdentifierFormatException("Invalid Post Vote Id: " + id);
        }
    }

    public String stringify() {
        return id.toString();
    }
}
