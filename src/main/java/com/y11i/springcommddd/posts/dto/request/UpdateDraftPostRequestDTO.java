package com.y11i.springcommddd.posts.dto.request;

import com.y11i.springcommddd.posts.dto.internal.PostAssetUploadDTO;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public record UpdateDraftPostRequestDTO(
        @Nullable
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "communityId must be a UUID string"
        )
        String communityId,

        @Nullable
        @Size(max = 120)
        String title,

        @Nullable
        @Size(max = 20000)
        String content,

        @Nullable
        @Size(max = 1024)
        @URL(
                regexp = "^(http|https)://.*$",
                message = "link must be a valid http/https URL"
        )
        String link,

        @Nullable
        List<PostAssetUploadDTO> postAssetUploadDTOs
) {
}
