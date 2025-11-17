package com.y11i.springcommddd.posts.application.service;

import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.exception.CommunityNotFoundException;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.exception.MemberNotFoundException;
import com.y11i.springcommddd.posts.application.port.in.GetPostDetailUseCase;
import com.y11i.springcommddd.posts.application.port.out.LoadAuthorForPostPort;
import com.y11i.springcommddd.posts.application.port.out.LoadCommunityForPostPort;
import com.y11i.springcommddd.posts.application.port.out.LoadPostAssetsPort;
import com.y11i.springcommddd.posts.application.port.out.LoadPostPort;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.exception.PostNotFound;
import com.y11i.springcommddd.posts.dto.internal.MediaVariantDTO;
import com.y11i.springcommddd.posts.dto.internal.PostAuthorDTO;
import com.y11i.springcommddd.posts.dto.internal.PostCommunityDTO;
import com.y11i.springcommddd.posts.dto.internal.PostMediaAssetDTO;
import com.y11i.springcommddd.posts.dto.response.PostDetailResponseDTO;
import com.y11i.springcommddd.posts.media.domain.MediaVariant;
import com.y11i.springcommddd.posts.media.domain.PostAsset;
import com.y11i.springcommddd.votes.domain.PostVote;
import com.y11i.springcommddd.votes.domain.PostVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 게시글 상세 조회 애플리케이션 서비스 구현체.
 *
 * <p><b>역할</b></p>
 * <ul>
 *     <li>Post 애그리게잇 로드</li>
 *     <li>커뮤니티/작성자 정보 로드 후 내부 DTO로 매핑</li>
 *     <li>투표 집계(up/down, score), 댓글 수(commentCount) 제공</li>
 *     <li>현재 사용자(viewer)의 투표 상태(myVote) 제공</li>
 *     <li>MEDIA 게시글일 경우 미디어 자산 목록(mediaAssets) 제공</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetPostDetailService implements GetPostDetailUseCase {
    private final LoadPostPort loadPostPort;
    private final LoadCommunityForPostPort loadCommunityForPostPort;
    private final LoadAuthorForPostPort loadAuthorForPostPort;
    private final LoadPostAssetsPort loadPostAssetsPort;
    private final PostVoteRepository postVoteRepository;

    /**
     * 게시글 상세 정보를 조회한다.
     *
     * @param postId   게시글 ID
     * @param viewerId 조회자 ID (로그인 사용자, 비로그인 열고 싶으면 null 허용하도록 확장 가능)
     * @return 게시글 상세 응답 DTO
     */
    @Override
    public PostDetailResponseDTO getPostDetail(PostId postId, MemberId viewerId) {
        // 1. Load Post
        Post post = loadPostPort.loadById(postId)
                .orElseThrow(() -> new PostNotFound(postId.stringify()));

        // 2. Load Community
        Community community = loadCommunityForPostPort.loadById(post.communityId())
                .orElseThrow(() -> new CommunityNotFoundException(post.communityId().stringify()));

        // Load Author info
        Member author = loadAuthorForPostPort.loadById(post.authorId())
                .orElseThrow(() -> new MemberNotFoundException(post.authorId().stringify()));

        //3. My Vote status (-1 / 0 / 1)
        Integer myVote = null;
        if (viewerId != null) {
            myVote = postVoteRepository.findByPostIdAndVoterId(postId, viewerId)
                    .map(PostVote::value)
                    .orElse(0);
        }

        // 4. Load Media Assets(Nullable)
        List<PostAsset> postAssets = loadPostAssetsPort.loadByPostId(post.postId());
        List<PostMediaAssetDTO> mediaAssetDTOs = postAssets.stream()
                .map(this::toPostMediaAssetDTO)
                .toList();
        int upCount = post.upCount();
        int downCount = post.downCount();
        int score = post.score();

        boolean isEdited = post.updatedAt() != null
                && post.publishedAt() != null
                && !post.publishedAt().equals(post.updatedAt());


        // 5. Map Internal dto
        PostAuthorDTO postAuthorDTO = PostAuthorDTO.builder()
                .authorId(post.authorId().stringify())
                .authorDisplayName(author.displayName().value())
                .build();

        PostCommunityDTO postCommunityDTO = PostCommunityDTO.builder()
                .communityId(post.communityId().stringify())
                .communityName(community.communityName().value())
                .communityProfileImageUrl(community.profileImage().value())
                .build();

        // 6. Map Final DTO
        return PostDetailResponseDTO.builder()
                .author(postAuthorDTO)
                .postCommunity(postCommunityDTO)
                .postId(post.postId().stringify())
                .publishedAt(post.publishedAt())
                .isEdited(isEdited)
                .title(post.title().value())
                .content(post.content().value())
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

    private PostMediaAssetDTO toPostMediaAssetDTO(PostAsset postAsset) {
        return PostMediaAssetDTO.builder()
                .assetId(postAsset.postAssetId().stringify())
                .mediaType(postAsset.mediaType().toString())
                .displayOrder(postAsset.displayOrder())
                .srcUrl(postAsset.srcUrl().value())
                .mimeType(postAsset.mimeType())
                .width(postAsset.width())
                .height(postAsset.height())
                .durationSec(postAsset.durationSec())
                .altText(postAsset.altText())
                .caption(postAsset.caption())
                .processingStatus(postAsset.processingStatus().toString())
                .processingError(postAsset.processingError())
                .variants(
                        postAsset.variants()
                                .stream()
                                .map(
                                        v -> MediaVariantDTO.builder()
                                                .name(v.name())
                                                .url(v.url().value())
                                                .mimeType(v.mimeType())
                                                .width(v.width())
                                                .height(v.height())
                                                .build()
                                )
                                .toList()
                )
                .build();
    }
}
