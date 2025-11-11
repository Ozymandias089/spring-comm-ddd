package com.y11i.springcommddd.posts.dto;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

public class PostDTO {
    @Getter
    private UUID id;
    @Getter
    private CommunityId communityId;
    @Getter
    private MemberId authorId;
    @Getter
    private String title;
    @Getter
    private String content;
    @Getter
    private String status;
    @Getter
    private Instant publishedAt;
    @Getter
    private Instant createdAt;
    @Getter
    private Instant updatedAt;
    @Getter
    private int up;
    @Getter
    private int down;
    @Getter
    private int score;

    @Builder
    public PostDTO(UUID id, CommunityId communityId, MemberId authorId, String title, String content, String status, Instant publishedAt, Instant updatedAt, int up, int down, int score) {
        this.id = id;
        this.communityId = communityId;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.status = status;
        this.publishedAt = publishedAt;
        this.updatedAt = updatedAt;
        this.up = up;
        this.down = down;
        this.score = score;
    }
}
