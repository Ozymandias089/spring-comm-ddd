package com.y11i.springcommddd.posts.dto.internal;

import com.y11i.springcommddd.iam.domain.Member;
import lombok.Builder;

public record PostAuthorDTO(String authorId, String authorDisplayName) {
    @Builder
    public PostAuthorDTO {}

    public static PostAuthorDTO from(Member author) {
        return PostAuthorDTO.builder()
                .authorId(author.memberId().stringify())
                .authorDisplayName(author.displayName().value())
                .build();
    }
}
