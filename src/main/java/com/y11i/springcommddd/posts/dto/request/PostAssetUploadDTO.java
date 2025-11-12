package com.y11i.springcommddd.posts.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PostAssetUploadDTO(

        @NotBlank              // "IMAGE" | "VIDEO"
        String mediaType,

        @NotNull
        Integer displayOrder,

        @NotNull @Positive
        Long fileSize,      // 바이트 단위 (long 타입)

        @NotBlank @Size(max = 255)
        String fileName,    // 업로드된 파일명 (key 또는 원본명)

        @Size(max = 255)
        String mimeType     // 예: image/jpeg, video/mp4
) {}
