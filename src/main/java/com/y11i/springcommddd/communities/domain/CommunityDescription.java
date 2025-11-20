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
        if (value != null && value.isBlank()) throw new IllegalArgumentException("Description cannot be blank if provided");
        if (value != null && value.length() > 1000) throw new IllegalArgumentException("Description length must be <= 1000");
        this.value = value;
    }

    public String value() {
        return value;
    }
}
