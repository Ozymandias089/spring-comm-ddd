package com.y11i.springcommddd.communities.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CommunityName implements ValueObject {
    @Column(name = "name", nullable = false, length = 100)
    private String value;

    protected CommunityName() {}

    public CommunityName(String value) {
        if (value == null) throw new IllegalArgumentException("name required");
        String v = value.trim();
        if (v.isBlank()) throw new IllegalArgumentException("name cannot be blank");
        if (v.length() > 100) throw new IllegalArgumentException("name too long");
        this.value = v;
    }

    public String value() { return value; }

    @Override public boolean equals(Object o){ return o instanceof CommunityName n && value.equals(n.value); }
    @Override public int hashCode(){ return value.hashCode(); }
}
