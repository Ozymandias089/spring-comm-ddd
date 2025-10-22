package com.y11i.springcommddd.posts.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;

@Embeddable
public class Content implements ValueObject {
    @Lob
    private String value;

    protected Content() {}

    public Content(String value) {
        if(value == null || value.isBlank()) throw new IllegalArgumentException("Content value cannot be null or blank");
        this.value = value;
    }

    public String value() { return value; }
}
