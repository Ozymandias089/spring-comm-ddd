package com.y11i.springcommddd.posts.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Title implements ValueObject {
    @Column(name = "title", nullable = false, length = 200)
    private String value;

    /** For JPA Auditing */
    protected Title() {}

    public Title(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("title cannot be null or blank");
        if (value.length() > 200) throw new IllegalArgumentException("Title cannot be longer than 200 characters");
        this.value = value.trim();
    }

    public String value() { return value; }
}
