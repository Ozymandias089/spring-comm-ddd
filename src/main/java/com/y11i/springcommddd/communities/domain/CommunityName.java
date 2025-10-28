package com.y11i.springcommddd.communities.domain;

import com.y11i.springcommddd.communities.domain.exception.InvalidCommunityName;
import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * 커뮤니티 표시명 값 객체.
 *
 * <p><b>검증 규칙</b></p>
 * <ul>
 *   <li>null 금지</li>
 *   <li>트림 후 공백 금지</li>
 *   <li>최대 길이 100자</li>
 * </ul>
 *
 * <p>표시명은 사람이 읽기 쉬운 이름이며, URL/고유 접근에는 {@link CommunityNameKey}를 사용합니다.</p>
 */
@Embeddable
public class CommunityName implements ValueObject {
    @Column(name = "name", nullable = false, length = 100)
    private String value;

    /** JPA 기본 생성자. 외부에서 직접 호출하지 않습니다. */
    protected CommunityName() {}

    /**
     * 표시명을 받아 값 객체를 생성합니다.
     *
     * @param value 표시명
     * @throws InvalidCommunityName null/공백/과다 길이(100 초과)인 경우
     */
    public CommunityName(String value) {
        if (value == null) throw new InvalidCommunityName("name required");
        String v = value.trim();
        if (v.isBlank()) throw new InvalidCommunityName("name cannot be blank");
        if (v.length() > 100) throw new InvalidCommunityName("name too long");
        this.value = v;
    }

    /** 표시명 문자열 */
    public String value() { return value; }

    @Override public boolean equals(Object o){ return o instanceof CommunityName n && value.equals(n.value); }
    @Override public int hashCode(){ return value.hashCode(); }
}
