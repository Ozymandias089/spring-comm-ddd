package com.y11i.springcommddd.communities.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CommunityDescription implements ValueObject {
    @Column(name = "description", length = 1000)
    private String value;

    protected CommunityDescription() {}

    public CommunityDescription(String value) {
        if (value == null) {
            this.value = null;
            return;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException("Description cannot be blank if provided");
        if (trimmed.length() > 1000) throw new IllegalArgumentException("Description length must be <= 1000");
        this.value = trimmed;
    }

    public String value() {
        return value;
    }
}
