package com.y11i.springcommddd.posts.dto.internal;

import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.media.domain.MediaType;
import com.y11i.springcommddd.posts.media.domain.PostAssetId;
import com.y11i.springcommddd.posts.media.domain.ProcessingStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

/**
 * 내부용 게시글 자산 DTO.
 * <p>URL, MIME 등은 도메인 값 그대로 전달하고, 별도 직렬화 처리는 고려하지 않습니다.</p>
 */
public class PostAssetDTO {

    @Getter
    private PostAssetId postAssetId;
    @Getter
    private PostId postId;

    @Getter
    private MediaType mediaType;
    @Getter
    private int displayOrder;

    @Getter
    private String srcUrl;      // Url VO를 그대로 써도 되지만, 내부 통신에선 문자열도 허용
    @Getter
    private String mimeType;
    @Getter
    private Integer width;
    @Getter
    private Integer height;
    @Getter
    private Integer durationSec;

    @Getter
    private String altText;
    @Getter
    private String caption;

    @Getter
    private Long sizeBytes;
    @Getter
    private String originalFilename;

    @Getter
    private ProcessingStatus processingStatus;
    @Getter
    private String processingError;

    @Getter
    private List<MediaVariantDTO> variants;

    @Builder
    public PostAssetDTO(
            PostAssetId postAssetId,
            PostId postId,
            MediaType mediaType,
            int displayOrder,
            String srcUrl,
            String mimeType,
            Integer width,
            Integer height,
            Integer durationSec,
            String altText,
            String caption,
            Long sizeBytes,
            String originalFilename,
            ProcessingStatus processingStatus,
            String processingError,
            List<MediaVariantDTO> variants
    ) {
        this.postAssetId = Objects.requireNonNull(postAssetId);
        this.postId = Objects.requireNonNull(postId);
        this.mediaType = Objects.requireNonNull(mediaType);
        this.displayOrder = displayOrder;
        this.srcUrl = srcUrl;
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
        this.durationSec = durationSec;
        this.altText = altText;
        this.caption = caption;
        this.sizeBytes = sizeBytes;
        this.originalFilename = originalFilename;
        this.processingStatus = Objects.requireNonNull(processingStatus);
        this.processingError = processingError;
        this.variants = variants == null ? List.of() : List.copyOf(variants);
    }
}