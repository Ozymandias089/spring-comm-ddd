package com.y11i.springcommddd.communities.dto.request;

import jakarta.annotation.Nullable;

public record ChangeImagesRequestDTO(
        @Nullable String profileImageUrl,
        @Nullable String bannerImageUrl
) {
}
