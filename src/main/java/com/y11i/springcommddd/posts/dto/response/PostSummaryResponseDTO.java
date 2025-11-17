package com.y11i.springcommddd.posts.dto.response;

import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.posts.domain.Post;
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

    /**
     * Post + Community + Author + myVote 를 요약 응답 DTO로 변환한다.
     */
    public static PostSummaryResponseDTO from(
            Post post,
            Community community,
            Member author,
            Integer myVote
    ) {
        int upCount = post.upCount();
        int downCount = post.downCount();
        int score = post.score();

        Instant publishedAt = post.publishedAt();
        boolean isEdited = publishedAt != null
                && post.updatedAt() != null
                && !publishedAt.equals(post.updatedAt());

        PostAuthorDTO authorDTO = PostAuthorDTO.from(author);
        PostCommunityDTO communityDTO = PostCommunityDTO.from(community);

        String contentPreview = buildContentPreview(post);

        return PostSummaryResponseDTO.builder()
                .author(authorDTO)
                .community(communityDTO)
                .postId(post.postId().stringify())
                .title(post.title().value())
                .contentPreview(contentPreview)
                .kind(post.kind().name())
                .upCount(upCount)
                .downCount(downCount)
                .score(score)
                .commentCount(post.commentCount())
                .myVote(myVote)
                .publishedAt(publishedAt)
                .isEdited(isEdited)
                .build();
    }

    private static String buildContentPreview(Post post) {
        if (post.content() == null || post.content().value() == null) return "";
        String full = post.content().value();
        return (full.length() <= 200) ? full : full.substring(0, 200);
    }
}
