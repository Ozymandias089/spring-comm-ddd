package com.y11i.springcommddd.posts.dto.response;

import com.y11i.springcommddd.posts.dto.internal.PostAuthorDTO;
import com.y11i.springcommddd.posts.dto.internal.PostCommunityDTO;
import com.y11i.springcommddd.posts.dto.internal.PostMediaAssetDTO;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

public record PostDetailResponseDTO(PostAuthorDTO author, PostCommunityDTO postCommunity, String postId,
                                    Instant createdAt, Instant updatedAt, String title, String content, String kind,
                                    String status, int upCount, int downCount, int score, int commentCount,
                                    Integer myVote, List<PostMediaAssetDTO> mediaAssets) {
    @Builder
    public PostDetailResponseDTO {
        mediaAssets = mediaAssets == null ? List.of() : List.copyOf(mediaAssets);
    }
}
