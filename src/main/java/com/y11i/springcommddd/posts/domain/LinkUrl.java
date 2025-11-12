package com.y11i.springcommddd.posts.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.net.URI;

@Embeddable
public class LinkUrl implements ValueObject {
    @Column(name = "link_url", length = 1024)
    private String value;

    protected LinkUrl() {}

    public LinkUrl(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("linkUrl cannot be null or blank");
        }
        // 기본적인 URI 형태 검증(너무 빡세지 않게)
        try { URI.create(value); } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("invalid linkUrl");
        }
        this.value = value.trim();
    }

    public String value() { return value; }
}
