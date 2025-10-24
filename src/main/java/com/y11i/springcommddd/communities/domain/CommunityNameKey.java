package com.y11i.springcommddd.communities.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * 커뮤니티 이름 키(정규화된 고유 키) 값 객체.
 *
 * <p><b>의도</b><br>
 * URL/슬러그/검색 키로 사용되는 정규화된 문자열입니다.
 * 테이블에서는 유니크 제약을 가집니다.
 * </p>
 *
 * <p><b>정규화 규칙</b></p>
 * <ul>
 *   <li>소문자화</li>
 *   <li>연속 공백 → 밑줄('_')</li>
 *   <li>허용 문자: {@code a-z}, {@code 0-9}, {@code _}</li>
 *   <li>길이: 3~32</li>
 * </ul>
 */
@Embeddable
public class CommunityNameKey implements ValueObject {
    @Column(name = "name_key", nullable = false, length = 32)
    private String value;

    /** JPA 기본 생성자. 외부에서 직접 호출하지 않습니다. */
    protected CommunityNameKey() {}

    /**
     * 원문 문자열을 정규화하여 이름 키를 생성합니다.
     *
     * @param raw 원문 문자열
     * @throws IllegalArgumentException null 이거나, 정규화 결과가 패턴/길이를 만족하지 않는 경우
     */
    public CommunityNameKey(String raw) {
        String k = normalize(raw);
        if (!k.matches("[a-z0-9_]{3,32}")) {
            throw new IllegalArgumentException("invalid community name (allowed: a-z, 0-9, _; 3..32)");
        }
        this.value = k;
    }

    /**
     * 이름 키 정규화 함수.
     *
     * @param raw 원문
     * @return 정규화된 문자열(소문자, 공백→'_', 허용 외 문자 제거)
     * @throws IllegalArgumentException raw가 null인 경우
     */
    public static String normalize(String raw) {
        if (raw == null) throw new IllegalArgumentException("raw name required");
        String s = raw.trim().toLowerCase();
        // 예: 공백은 '_'로, 허용하지 않는 문자는 제거
        s = s.replaceAll("\\s+", "_");
        s = s.replaceAll("[^a-z0-9_]", "");
        return s;
    }

    /** 정규화된 이름 키 문자열 */
    public String value(){ return value; }

    @Override public boolean equals(Object o){ return o instanceof CommunityNameKey k && value.equals(k.value); }
    @Override public int hashCode(){ return value.hashCode(); }
}
