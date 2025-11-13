package com.y11i.springcommddd.posts.dto.internal;

import lombok.Builder;
import lombok.Getter;

public final class PostAuthorDTO {
    @Getter private final String authorId;
    @Getter private final String authorDisplayName;

    @Builder
    public PostAuthorDTO(String authorId, String authorDisplayName) {
        this.authorId = authorId;
        this.authorDisplayName = authorDisplayName;
    }
}
