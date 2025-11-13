package com.y11i.springcommddd.posts.dto.internal;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public final class PostMediaAssetDTO {
    @Getter private final String assetId;
    @Getter private final String mediaType;
    @Getter private final int displayOrder;
    @Getter private final String srcUrl;
    @Getter private final String mimeType;
    @Getter private final Integer width;
    @Getter private final Integer height;
    @Getter private final Integer durationSec;
    @Getter private final String altText;
    @Getter private final String caption;

    /** 처리 상태: "READY" | "PROCESSING" | "FAILED" (ProcessingStatus enum 평탄화) */
    @Getter private final String processingStatus;
    @Getter private final String processingError;

    /**
     * 파생본(variants) 목록.
     * - 예: small, large, poster, hls, mp4_720 등
     * - 필요 없으면 빈 리스트 또는 null
     */
    @Getter private final List<MediaVariantDTO> variants;

    @Builder
    public PostMediaAssetDTO(
            String assetId, String mediaType, int displayOrder,
            String srcUrl, String mimeType, Integer width, Integer height,
            Integer durationSec, String altText, String caption,
            String processingStatus, String processingError, List<MediaVariantDTO> variants
    ) {
        this.assetId = assetId;
        this.mediaType = mediaType;
        this.displayOrder = displayOrder;
        this.srcUrl = srcUrl;
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
        this.durationSec = durationSec;
        this.altText = altText;
        this.caption = caption;
        this.processingStatus = processingStatus;
        this.processingError = processingError;
        this.variants = variants;
    }
}
