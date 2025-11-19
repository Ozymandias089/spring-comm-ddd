package com.y11i.springcommddd.posts.media.model;

import com.y11i.springcommddd.posts.media.domain.MediaType;

/**
 * 업로드된 미디어 자산 메타정보.
 * (컨트롤러에서 PostAssetUploadDTO -> AssetMeta로 변환해서 넘겨주면 됨)
 */
public record AssetMeta(
        MediaType mediaType,  // IMAGE | VIDEO
        int displayOrder,
        long fileSize,
        String fileName,
        String mimeType
) {
}
