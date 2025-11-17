package com.y11i.springcommddd.posts.dto.internal;

import lombok.Builder;

public record PostAuthorDTO(String authorId, String authorDisplayName) {
    @Builder
    public PostAuthorDTO {}
}
