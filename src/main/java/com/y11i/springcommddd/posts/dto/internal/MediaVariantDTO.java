package com.y11i.springcommddd.posts.dto.internal;

import lombok.Builder;
import lombok.Getter;

public class MediaVariantDTO {
    @Getter
    private String name;
    @Getter
    private String url;
    @Getter
    private String mimeType;
    @Getter
    private Integer width;
    @Getter
    private Integer height;

    @Builder
    public MediaVariantDTO(String name, String url, String mimeType, Integer width, Integer height) {
        this.name = name;
        this.url = url;
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
    }
}