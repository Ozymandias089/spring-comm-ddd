package com.y11i.springcommddd.posts.dto.internal;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.PostKind;
import com.y11i.springcommddd.posts.domain.PostStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * 게시글 응답 DTO.
 * <p>식별자는 UUID 문자열이 아니라 도메인 타입(PostId/CommunityId/MemberId) 그대로 노출합니다.</p>
 */
public class PostDTO {
    @Getter
    private PostId postId;
    @Getter
    private CommunityId communityId;
    @Getter
    private MemberId authorId;

    @Getter
    private String title;    // 저장은 Title VO로 하지만 응답은 문자열
    @Getter
    private String content;  // 텍스트 게시글 또는 미디어 캡션용(없으면 null)
    @Getter
    private String link;     // 링크 게시글 전용(없으면 null)

    @Getter
    private PostKind postType;     // TEXT | LINK | MEDIA ...
    @Getter
    private PostStatus status;     // DRAFT | PUBLISHED | ARCHIVED ...

    @Getter
    private Instant createdAt;
    @Getter
    private Instant updatedAt;

    /** 첨부 자산(이미지/영상). 미디어가 없는 게시글이면 빈 리스트 */
    @Getter
    private List<PostAssetDTO> assets;

    @Builder
    public PostDTO(
            PostId postId,
            CommunityId communityId,
            MemberId authorId,
            String title,
            String content,
            String link,
            PostKind postType,
            PostStatus status,
            Instant createdAt,
            Instant updatedAt,
            List<PostAssetDTO> assets
    ) {
        this.postId = postId;
        this.communityId = communityId;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.link = link;
        this.postType = postType;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.assets = assets;
    }
}
