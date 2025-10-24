package com.y11i.springcommddd.communities.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;
import java.util.UUID;

/**
 * 커뮤니티 식별자 값 객체.
 *
 * <p><b>특징</b></p>
 * <ul>
 *   <li>UUID 기반 임베디드 값 객체</li>
 *   <li>DB 컬럼 타입: {@code BINARY(16)}</li>
 *   <li>불변(Immutable)</li>
 * </ul>
 */
@Embeddable
public record CommunityId(
        @Column(name="community_id", columnDefinition="BINARY(16)", nullable=false, updatable=false)
        UUID id
) implements ValueObject {

    /**
     * 생성자 유효성 검증: {@code id}는 null일 수 없습니다.
     *
     * @param id UUID 값
     * @throws NullPointerException id가 null인 경우
     */
    public CommunityId { Objects.requireNonNull(id); }

    /**
     * 무작위 UUID로 새로운 커뮤니티 식별자를 생성합니다.
     *
     * @return 새 {@link CommunityId}
     */
    public static CommunityId newId() {return new CommunityId(UUID.randomUUID());}
}
