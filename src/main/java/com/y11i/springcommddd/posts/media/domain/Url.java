package com.y11i.springcommddd.posts.media.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Url implements ValueObject {
    @Column(name = "url", nullable = false, length = 1024)
    private String url;

    protected Url() {}

    public Url(String url) {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("Url cannot be null or blank");
        this.url = url.trim();
    }

    public String value() { return url; }
}
