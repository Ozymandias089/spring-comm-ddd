package com.y11i.springcommddd.posts.dto.response;

import com.y11i.springcommddd.posts.dto.internal.PostAuthorDTO;
import com.y11i.springcommddd.posts.dto.internal.PostCommunityDTO;
import lombok.Builder;

import java.time.Instant;

public record PostSummaryResponseDTO(
        PostAuthorDTO author,
        PostCommunityDTO community,
        String postId,
        String title,
        String contentPreview,
        String kind,
        int upCount,
        int downCount,
        int score,
        int commentCount,
        Integer myVote,
        Instant publishedAt,
        boolean isEdited
) {
    @Builder
    public PostSummaryResponseDTO{}
}
