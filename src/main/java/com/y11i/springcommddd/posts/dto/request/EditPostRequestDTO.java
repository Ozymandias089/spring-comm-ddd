package com.y11i.springcommddd.posts.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

/**
 * 게시글 수정 요청 DTO.
 *
 * <p>
 *  - title, content 둘 중 하나만 보내도 되고, 둘 다 보내도 된다.
 *  - null인 필드는 수정하지 않는다.
 * </p>
 */
public record EditPostRequestDTO(
        @Nullable
        @Size(max = 200)
        String title,

        @Nullable
        String content
) {
}