package com.y11i.springcommddd.communities.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CommunityNameKey implements ValueObject {
    @Column(name = "name_key", nullable = false, length = 32)
    private String value;

    protected CommunityNameKey() {}

    public CommunityNameKey(String raw) {
        String k = normalize(raw);
        if (!k.matches("[a-z0-9_]{3,32}")) {
            throw new IllegalArgumentException("invalid community name (allowed: a-z, 0-9, _; 3..32)");
        }
        this.value = k;
    }

    public static String normalize(String raw) {
        if (raw == null) throw new IllegalArgumentException("raw name required");
        String s = raw.trim().toLowerCase();
        // 예: 공백은 '_'로, 허용하지 않는 문자는 제거
        s = s.replaceAll("\\s+", "_");
        s = s.replaceAll("[^a-z0-9_]", "");
        return s;
    }

    public String value(){ return value; }

    @Override public boolean equals(Object o){ return o instanceof CommunityNameKey k && value.equals(k.value); }
    @Override public int hashCode(){ return value.hashCode(); }
}
