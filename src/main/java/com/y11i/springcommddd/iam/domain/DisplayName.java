package com.y11i.springcommddd.iam.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class DisplayName implements ValueObject {
    @Column(name = "display_name", nullable = false, length = 50)
    private String value;

    protected DisplayName() {}

    public DisplayName(String value) {
        if (value == null) throw new IllegalArgumentException("DisplayName value cannot be null");
        String trimmedValue = value.trim();
        if (trimmedValue.isBlank()) throw new IllegalArgumentException("DisplayName value cannot be blank");
        if (trimmedValue.length() < 2) throw new IllegalArgumentException("DisplayName value must be between 2..50");
        this.value = trimmedValue;
    }

    public String value() { return value; }
}
