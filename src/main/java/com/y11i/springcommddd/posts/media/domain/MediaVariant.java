package com.y11i.springcommddd.posts.media.domain;

import com.y11i.springcommddd.shared.domain.ValueObject;
import jakarta.persistence.*;

import java.util.Objects;

@Access(AccessType.FIELD)
@Embeddable
public class MediaVariant implements ValueObject {
    /** 파생본 논리 이름: small/medium/large/poster/hls/mp4_720 등 */
    @Column(name = "variant_name", nullable = false, length = 64)
    private String name;

    /** 접근 URL (서명 URL을 바로 쓰지 않는다면 StorageKey로 교체해도 됨) */
    @Embedded
    @AttributeOverride(name = "url", column = @Column(name = "variant_url", nullable = false, length = 1024))
    private Url url;

    /** 선택 메타 */
    @Column(name = "variant_mime_type", length = 255)
    private String mimeType;
    @Column(name = "variant_width")  private Integer width;
    @Column(name = "variant_height") private Integer height;

    protected MediaVariant() {}
    public MediaVariant(String name, Url url, String mimeType, Integer width, Integer height) {
        this.name = Objects.requireNonNull(name).trim();
        if (this.name.isEmpty()) throw new IllegalArgumentException("variant name must not be blank");
        this.url = Objects.requireNonNull(url);
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
    }

    public String name() { return name; }
    public Url url() { return url; }
    public String mimeType() { return mimeType; }
    public Integer width() { return width; }
    public Integer height() { return height; }
}
