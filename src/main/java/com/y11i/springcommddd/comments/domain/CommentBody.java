package com.y11i.springcommddd.comments.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;

@Embeddable
public class CommentBody implements ValueObject {
    @Lob
    private String value;

    protected  CommentBody() {
    }
    public CommentBody(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Comment body cannot be null or blank");
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override public boolean equals(Object o) { return o instanceof CommentBody b && value.equals(b.value); }
    @Override public int hashCode() { return value.hashCode(); }
}
