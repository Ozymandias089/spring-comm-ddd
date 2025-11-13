package com.y11i.springcommddd.posts.dto.response;

import com.y11i.springcommddd.posts.dto.internal.PostAuthorDTO;
import com.y11i.springcommddd.posts.dto.internal.PostCommunityDTO;
import com.y11i.springcommddd.posts.dto.internal.PostMediaAssetDTO;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

public final class PostMediaResponseDTO {
    @Getter private final PostCommunityDTO postCommunity;
    @Getter private final PostAuthorDTO postAuthor;

    @Getter private final String postId;
    @Getter private final Instant createdAt;
    @Getter private final Instant updatedAt;

    @Getter private final String title;
    @Getter private final List<PostMediaAssetDTO> assets;

    public PostMediaResponseDTO(PostCommunityDTO postCommunity, PostAuthorDTO postAuthor, String postId, Instant createdAt, Instant updatedAt, String title, List<PostMediaAssetDTO> assets) {
        this.postCommunity = postCommunity;
        this.postAuthor = postAuthor;
        this.postId = postId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.title = title;
        this.assets = assets;
    }
}
