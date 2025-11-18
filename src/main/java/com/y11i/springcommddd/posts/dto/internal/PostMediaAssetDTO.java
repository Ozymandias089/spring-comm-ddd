package com.y11i.springcommddd.posts.dto.internal;

import com.y11i.springcommddd.posts.media.domain.PostAsset;
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
    public PostMediaAssetDTO {
        variants = (variants == null) ? List.of() : List.copyOf(variants);
    }

    public static PostMediaAssetDTO from(PostAsset postAsset) {
        return PostMediaAssetDTO.builder()
                .assetId(postAsset.postAssetId().stringify())
                .mediaType(postAsset.mediaType().toString())
                .displayOrder(postAsset.displayOrder())
                .srcUrl(postAsset.srcUrl().value())
                .mimeType(postAsset.mimeType())
                .width(postAsset.width())
                .height(postAsset.height())
                .durationSec(postAsset.durationSec())
                .altText(postAsset.altText())
                .caption(postAsset.caption())
                .processingStatus(postAsset.processingStatus().toString())
                .processingError(postAsset.processingError())
                .variants(
                        postAsset.variants().stream()
                                .map(v -> MediaVariantDTO.builder()
                                        .name(v.name())
                                        .url(v.url().value())
                                        .mimeType(v.mimeType())
                                        .width(v.width())
                                        .height(v.height())
                                        .build()
                                )
                                .toList()
                )
                .build();
    }
}
