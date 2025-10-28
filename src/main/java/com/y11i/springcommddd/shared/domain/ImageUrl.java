package com.y11i.springcommddd.shared.domain;

import com.y11i.springcommddd.shared.domain.exception.InvalidImageUrl;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ImageUrl implements ValueObject {
    @Column(name = "url", nullable = false, length = 1024)
    private String url;

    protected ImageUrl() {
    }

    public ImageUrl(String url) {
        if (url == null || url.isBlank()) throw new InvalidImageUrl("url cannot be null or blank");
        String v = url.trim();

        // (선택) 아주 가벼운 포맷 검증: http/https 로 시작 + 공백 없음
        if (!(v.startsWith("http://") || v.startsWith("https://")))
            throw new InvalidImageUrl("url must start with http:// or https://");
        if (v.contains(" "))
            throw new InvalidImageUrl("url must not contain spaces");
        this.url = v;
    }

    public String value() { return url; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageUrl other)) return false;
        return url.equals(other.url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }
}
