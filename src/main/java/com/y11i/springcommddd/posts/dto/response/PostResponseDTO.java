package com.y11i.springcommddd.posts.dto.response;

import com.y11i.springcommddd.posts.dto.internal.PostAuthorDTO;
import com.y11i.springcommddd.posts.dto.internal.PostCommunityDTO;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

public final class PostResponseDTO {// TODO: DTO 쪼개기, 텍스트가 아닌 링크나 미디어인 경우 생각하기
    @Getter private final PostCommunityDTO postCommunity;
    @Getter private final PostAuthorDTO postAuthor;
    @Getter private final String postId;
    @Getter private final Instant createdDate;
    @Getter private final Instant modifiedDate;
    @Getter private final String title;
    @Getter private final String content;

    @Getter private final int score;
    @Getter private final long commentCount;

    @Builder
    public PostResponseDTO(
            PostCommunityDTO postCommunity, PostAuthorDTO postAuthor,
            String postId, Instant createdDate, Instant modifiedDate,
            String title, String content, int score, long commentCount
    ) {
        this.postCommunity = postCommunity;
        this.postAuthor = postAuthor;
        this.postId = postId;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.title = title;
        this.content = content;
        this.score = score;
        this.commentCount = commentCount;
    }
}
