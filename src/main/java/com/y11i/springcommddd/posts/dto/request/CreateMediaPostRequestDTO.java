package com.y11i.springcommddd.posts.dto.request;

import com.y11i.springcommddd.posts.dto.internal.PostAssetUploadDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateMediaPostRequestDTO(
        @NotBlank
        @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "communityId must be a UUID string")
        String communityId,

        @NotBlank @Size(max = 120)
        String title,

        @Size(max = 100)
        String content,

        List<PostAssetUploadDTO> postAssetUploadDTOs
) {}
