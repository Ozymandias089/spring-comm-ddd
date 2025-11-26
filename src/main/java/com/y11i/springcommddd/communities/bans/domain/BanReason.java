package com.y11i.springcommddd.communities.bans.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class BanReason implements ValueObject {
    @Column(name = "ban_reason", length = 500)
    private String value;

    protected BanReason() {}

    public BanReason(String value) {
        if(value == null || value.isBlank()) {
            throw new IllegalArgumentException("BanReason value cannot be null or blank");
        }
        if(value.length() > 500) {
            throw new IllegalArgumentException("BanReason value cannot be longer than 500 characters");
        }
        this.value = value;
    }

    public String value() { return value; }
}
