package com.y11i.springcommddd.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ImageUrl implements ValueObject {
    @Column(name = "url", nullable = false, length = 1024)
    private String url;

    protected ImageUrl() {
    }

    public ImageUrl(String url) {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("url cannot be null or blank");
        this.url = url.trim();
    }

    public String value() { return url; }
}
