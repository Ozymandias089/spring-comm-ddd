package com.y11i.springcommddd.posts.dto;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

public class PostSummaryDTO {
    @Getter
    private UUID id;
    @Getter
    private CommunityId communityId;
    @Getter
    private MemberId authorId;
    @Getter
    private String title;
    @Getter
    private String status;
    @Getter
    private Instant publishedAt;
    @Getter
    private Instant createdAt;
    @Getter
    private int score;

    @Builder
    public PostSummaryDTO(UUID id, CommunityId communityId, MemberId authorId, String title, String status, Instant publishedAt, Instant createdAt, int score) {
        this.id = id;
        this.communityId = communityId;
        this.authorId = authorId;
        this.title = title;
        this.status = status;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
        this.score = score;
    }
}
