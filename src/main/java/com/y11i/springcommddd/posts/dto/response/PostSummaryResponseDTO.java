package com.y11i.springcommddd.posts.dto.response;

import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.dto.internal.PostAuthorDTO;
import com.y11i.springcommddd.posts.dto.internal.PostCommunityDTO;
import com.y11i.springcommddd.posts.dto.internal.PostMediaAssetDTO;
import com.y11i.springcommddd.posts.media.domain.PostAsset;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

public record PostSummaryResponseDTO(
        PostAuthorDTO author,
        PostCommunityDTO community,
        String postId,
        String title,
        String contentPreview,
        String linkUrl,
        List<PostMediaAssetDTO> assets,
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
            Integer myVote,
            List<PostAsset> postAssets
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

        List<PostMediaAssetDTO> mediaAssetDTOs = postAssets.stream()
                .map(PostMediaAssetDTO::from)
                .toList();
        String linkUrl = (post.linkUrl() != null) ? post.linkUrl().value() : null;

        return PostSummaryResponseDTO.builder()
                .author(authorDTO)
                .community(communityDTO)
                .postId(post.postId().stringify())
                .title(post.title().value())
                .contentPreview(contentPreview)
                .linkUrl(linkUrl)
                .assets(mediaAssetDTOs)
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
