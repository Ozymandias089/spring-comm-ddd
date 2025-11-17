package com.y11i.springcommddd.posts.dto.internal;

import lombok.Builder;

import java.util.List;

/**
 * @param processingStatus 처리 상태: "READY" | "PROCESSING" | "FAILED" (ProcessingStatus enum 평탄화)
 * @param variants         파생본(variants) 목록.
 *                         - 예: small, large, poster, hls, mp4_720 등
 *                         - 필요 없으면 빈 리스트 또는 null
 */
public record PostMediaAssetDTO(String assetId, String mediaType, int displayOrder, String srcUrl, String mimeType,
                                Integer width, Integer height, Integer durationSec, String altText, String caption,
                                String processingStatus, String processingError, List<MediaVariantDTO> variants) {
    @Builder
    public PostMediaAssetDTO {}
}
