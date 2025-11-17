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

public record PostDetailResponseDTO(PostAuthorDTO author, PostCommunityDTO postCommunity, String postId,
                                    Instant publishedAt, boolean isEdited, String title, String content, String kind,
                                    String status, int upCount, int downCount, int score, int commentCount,
                                    Integer myVote, List<PostMediaAssetDTO> mediaAssets) {
    @Builder
    public PostDetailResponseDTO {
        mediaAssets = mediaAssets == null ? List.of() : List.copyOf(mediaAssets);
    }

    public static PostDetailResponseDTO from(
            Post post,
            Community community,
            Member author,
            Integer myVote,
            List<PostAsset> assets
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

        List<PostMediaAssetDTO> mediaAssetDTOs = assets.stream()
                .map(PostMediaAssetDTO::from)
                .toList();

        String content = (post.content() != null) ? post.content().value() : null;

        return PostDetailResponseDTO.builder()
                .author(authorDTO)
                .postCommunity(communityDTO)
                .postId(post.postId().stringify())
                .publishedAt(publishedAt)
                .isEdited(isEdited)
                .title(post.title().value())
                .content(content)
                .kind(post.kind().toString())
                .status(post.status().toString())
                .upCount(upCount)
                .downCount(downCount)
                .score(score)
                .commentCount(post.commentCount())
                .myVote(myVote)
                .mediaAssets(mediaAssetDTOs)
                .build();
    }
}
