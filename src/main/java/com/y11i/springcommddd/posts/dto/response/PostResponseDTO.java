package com.y11i.springcommddd.posts.dto.response;


import lombok.Builder;

import java.time.Instant;

public class PostResponseDTO {// TODO: DTO 쪼개기, 텍스트가 아닌 링크나 미디어인 경우 생각하기
    private String communityId;
    private String communityName;
    private String communityProfileImageUrl;

    private String authorId;
    private String authorDisplayName;

    private String postId;
    private Instant createdDate;
    private Instant modifiedDate;
    private String title;
    private String content;

    private int score;
    private long commentCount;

    @Builder
    public PostResponseDTO(
            String communityId, String communityName, String communityProfileImageUrl,
            String authorId, String authorDisplayName,
            String postId, Instant createdDate, Instant modifiedDate,
            String title, String content, int score, long commentCount
    ) {
        this.communityId = communityId;
        this.communityName = communityName;
        this.communityProfileImageUrl = communityProfileImageUrl;
        this.authorId = authorId;
        this.authorDisplayName = authorDisplayName;
        this.postId = postId;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.title = title;
        this.content = content;
        this.score = score;
        this.commentCount = commentCount;
    }
}
