package com.y11i.springcommddd.posts.application.service;

import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.exception.CommunityNotFoundException;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.exception.MemberNotFoundException;
import com.y11i.springcommddd.posts.application.port.in.ListCommunityPostsUseCase;
import com.y11i.springcommddd.posts.application.port.in.ListHomeFeedPostsUseCase;
import com.y11i.springcommddd.posts.application.port.out.LoadAuthorForPostPort;
import com.y11i.springcommddd.posts.application.port.out.LoadCommunityForPostPort;
import com.y11i.springcommddd.posts.application.port.out.QueryPostPort;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.dto.internal.PageResultDTO;
import com.y11i.springcommddd.posts.dto.internal.PostAuthorDTO;
import com.y11i.springcommddd.posts.dto.internal.PostCommunityDTO;
import com.y11i.springcommddd.posts.dto.response.PostSummaryResponseDTO;
import com.y11i.springcommddd.votes.domain.MyPostVote;
import com.y11i.springcommddd.votes.domain.PostVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostFeedQueryService implements ListHomeFeedPostsUseCase, ListCommunityPostsUseCase {

    private final QueryPostPort queryPostPort;
    private final LoadAuthorForPostPort loadAuthorForPostPort;
    private final LoadCommunityForPostPort loadCommunityForPostPort;
    private final PostVoteRepository postVoteRepository;

    // ----------------------------------------------------
    // 홈 피드
    // ----------------------------------------------------
    @Override
    public PageResultDTO<PostSummaryResponseDTO> listHomeFeed(ListHomeFeedPostsUseCase.Query q) {
        PageRequest pageReq = PageRequest.of(q.page(), q.size());
        Page<Post> page = queryPostPort.findHomeFeed(q.sort(), pageReq);

        return buildPageResult(page, q.viewerId(), null);
    }

    // ----------------------------------------------------
    // 특정 커뮤니티 피드
    // ----------------------------------------------------
    @Override
    public PageResultDTO<PostSummaryResponseDTO> listCommunityPosts(ListCommunityPostsUseCase.Query q) {
        CommunityId communityId = CommunityId.objectify(q.communityId());

        // 커뮤니티 존재 여부 검증 + 캐싱용으로 로드
        Community community = loadCommunityForPostPort.loadById(communityId)
                .orElseThrow(() -> new CommunityNotFoundException(communityId.stringify()));

        PageRequest pageReq = PageRequest.of(q.page(), q.size());
        Page<Post> page = queryPostPort.findByCommunity(communityId, q.sort(), pageReq);

        return buildPageResult(page, q.viewerId(), community);
    }

    // ----------------------------------------------------
    // 공통 빌더
    // ----------------------------------------------------

    /**
     * Page<Post> → PageResultDTO<PostSummaryResponseDTO> 변환 공통 로직.
     *
     * @param page             조회된 게시글 페이지
     * @param viewerId         현재 사용자 (null 허용)
     * @param fixedCommunityOrNull 커뮤니티 피드의 경우 미리 로드한 Community, 홈 피드의 경우 null
     */
    private PageResultDTO<PostSummaryResponseDTO> buildPageResult(
            Page<Post> page,
            MemberId viewerId,
            Community fixedCommunityOrNull
    ) {
        List<Post> posts = page.getContent();
        if (posts.isEmpty()) {
            return new PageResultDTO<>(List.of(),
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.hasNext());
        }

        // 1) 내 투표값 일괄 조회
        Map<PostId, Integer> myVotesMap = resolveMyVotesMap(viewerId, posts);

        // 2) 커뮤니티 캐시 (홈 피드에서는 여러 커뮤니티가 섞일 수 있음)
        Map<CommunityId, Community> communityCache = new HashMap<>();
        if (fixedCommunityOrNull != null) {
            communityCache.put(fixedCommunityOrNull.communityId(), fixedCommunityOrNull);
        }

        // 3) Post → PostSummaryResponseDTO 매핑
        List<PostSummaryResponseDTO> summaries = posts.stream()
                .map(post -> {
                    Community community = resolveCommunity(post, communityCache);
                    Integer myVote = myVotesMap.get(post.postId());
                    return toSummaryDTO(post, community, myVote);
                })
                .toList();

        return new PageResultDTO<>(
                summaries,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }

    private Map<PostId, Integer> resolveMyVotesMap(MemberId viewerId, List<Post> posts) {
        if (viewerId == null || posts.isEmpty()) return Map.of();

        List<PostId> postIds = posts.stream()
                .map(Post::postId)
                .toList();

        List<MyPostVote> votes = postVoteRepository.findMyVotesByPostIds(viewerId, postIds);

        return votes.stream()
                .collect(Collectors.toMap(
                        MyPostVote::id,
                        MyPostVote::value
                ));
    }

    private Community resolveCommunity(Post post, Map<CommunityId, Community> cache) {
        CommunityId cid = post.communityId();
        Community cached = cache.get(cid);
        if (cached != null) return cached;

        Community loaded = loadCommunityForPostPort.loadById(cid)
                .orElseThrow(() -> new CommunityNotFoundException(cid.stringify()));
        cache.put(cid, loaded);
        return loaded;
    }

    private PostSummaryResponseDTO toSummaryDTO(Post post, Community community, Integer myVoteOrNull) {
        // 작성자 로드
        Member author = loadAuthorForPostPort.loadById(post.authorId())
                .orElseThrow(() -> new MemberNotFoundException(post.authorId().stringify()));

        PostAuthorDTO authorDTO = PostAuthorDTO.builder()
                .authorId(post.authorId().stringify())
                .authorDisplayName(author.displayName().value())
                .build();

        PostCommunityDTO communityDTO = PostCommunityDTO.builder()
                .communityId(community.communityId().stringify())
                .communityName(community.communityName().value())
                .communityProfileImageUrl(community.profileImage().value())
                .build();

        int upCount = post.upCount();
        int downCount = post.downCount();
        int score = post.score();

        Instant publishedAt = post.publishedAt();
        boolean isEdited = publishedAt != null
                && post.updatedAt() != null
                && !publishedAt.equals(post.updatedAt());

        return PostSummaryResponseDTO.builder()
                .author(authorDTO)
                .community(communityDTO)
                .postId(post.postId().stringify())
                .title(post.title().value())
                .contentPreview(buildContentPreview(post))
                .kind(post.kind().name())
                .upCount(upCount)
                .downCount(downCount)
                .score(score)
                .commentCount(post.commentCount())
                .myVote(myVoteOrNull)
                .publishedAt(publishedAt)
                .isEdited(isEdited)
                .build();
    }

    private String buildContentPreview(Post post) {
        if (post.content() == null || post.content().value() == null) return "";
        String full = post.content().value();
        return full.length() <= 200 ? full : full.substring(0, 200);
    }
}
