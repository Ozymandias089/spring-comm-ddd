package com.y11i.springcommddd.iam.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Email implements ValueObject {
    @Column(name = "email", nullable = false, length = 255)
    private String value;

    protected Email() {}

    public Email(String value) {
        if(value == null || value.isBlank()) throw new IllegalArgumentException("Email value cannot be null or blank");
        String trimmedValue = value.trim().toLowerCase();
        // 아주 간단한 형식 검사 (필요시 강화)
        if (!trimmedValue.contains("@") || trimmedValue.startsWith("@") || trimmedValue.endsWith("@"))
            throw new IllegalArgumentException("email format invalid");
        this.value = trimmedValue;
    }

    public String value() { return value; }
}
