package com.y11i.springcommddd.posts.application.service;

import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.domain.exception.CommunityNotFound;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.exception.MemberNotFound;
import com.y11i.springcommddd.posts.application.port.in.*;
import com.y11i.springcommddd.posts.application.port.out.LoadAuthorForPostPort;
import com.y11i.springcommddd.posts.application.port.out.LoadCommunityForPostPort;
import com.y11i.springcommddd.posts.application.port.out.LoadPostAssetsPort;
import com.y11i.springcommddd.posts.application.port.out.QueryPostPort;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.dto.internal.PageResultDTO;
import com.y11i.springcommddd.posts.dto.response.PostSummaryResponseDTO;
import com.y11i.springcommddd.posts.media.domain.PostAsset;
import com.y11i.springcommddd.votes.domain.MyPostVote;
import com.y11i.springcommddd.votes.domain.PostVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostFeedQueryService implements
        ListHomeFeedPostsUseCase,
        ListCommunityPostsUseCase,
        ListDraftsUseCase,
        SearchHomePostsUseCase,
        SearchCommunityPostsUseCase,
        SearchAuthorPostsUseCase
{

    private final QueryPostPort queryPostPort;
    private final LoadAuthorForPostPort loadAuthorForPostPort;
    private final LoadCommunityForPostPort loadCommunityForPostPort;
    private final PostVoteRepository postVoteRepository;
    private final LoadPostAssetsPort loadPostAssetsPort;

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
        CommunityNameKey nk= new CommunityNameKey(q.nameKey());

        // 커뮤니티 존재 여부 검증 + 캐싱용으로 로드
        Community community = loadCommunityForPostPort.loadByNameKey(nk)
                .orElseThrow(() -> new CommunityNotFound("Community not found: " + nk.value()));
        CommunityId communityId = community.communityId();

        // 2) 피드 조회
        PageRequest pageReq = PageRequest.of(q.page(), q.size());
        Page<Post> page = queryPostPort.findByCommunity(communityId, q.sort(), pageReq);

        // 3) 이미 로드한 community를 fixedCommunityOrNull로 넘겨 캐시 활용
        return buildPageResult(page, q.viewerId(), community);
    }

    @Override
    public PageResultDTO<PostSummaryResponseDTO> listDrafts(ListDraftsUseCase.Query q) {
        PageRequest pageReq = PageRequest.of(q.page(), q.size());
        Page<Post> page = queryPostPort.findDraftsByAuthorId(q.memberId(), q.sort(), pageReq);

        return buildPageResult(page, q.memberId(), null);
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

        // 3) 작성자 캐시 (동일 작성자가 여러 게시글을 쓴 경우 중복 조회 방지)
        Map<MemberId, Member> authorCache = new HashMap<>();

        // 3) Post → PostSummaryResponseDTO 매핑
        List<PostSummaryResponseDTO> summaries = posts.stream()
                .map(post -> {
                    Community community = resolveCommunity(post, communityCache);
                    Member author = resolveAuthor(post, authorCache);
                    Integer myVote = myVotesMap.get(post.postId());
                    List<PostAsset> assets = loadPostAssetsPort.loadByPostId(post.postId());
                    return PostSummaryResponseDTO.from(post, community, author, myVote, assets);
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
                .orElseThrow(() -> new CommunityNotFound(cid.stringify()));
        cache.put(cid, loaded);
        return loaded;
    }

    private Member resolveAuthor(Post post, Map<MemberId, Member> cache) {
        MemberId aid = post.authorId();
        Member cached = cache.get(aid);
        if (cached != null) return cached;

        Member loaded = loadAuthorForPostPort.loadById(aid)
                .orElseThrow(() -> new MemberNotFound("Member not found"));
        cache.put(aid, loaded);
        return loaded;
    }

    // ===========================
    // 검색: 특정 커뮤니티
    // ===========================

    @Override
    public PageResultDTO<PostSummaryResponseDTO> search(SearchCommunityPostsUseCase.Query q) {
        CommunityNameKey nk = new CommunityNameKey(q.nameKey());
        Community community = loadCommunityForPostPort.loadByNameKey(nk)
                .orElseThrow(() -> new CommunityNotFound("Community not found: " + nk.value()));
        CommunityId communityId = community.communityId();

        PageRequest pageReq = PageRequest.of(q.page(), q.size());
        Page<Post> page = queryPostPort.searchByCommunity(communityId, q.keyword(), q.sort(), pageReq);

        return buildPageResult(page, q.viewerId(), community);
    }

    // ===========================
    // 검색: 홈 전체
    // ===========================
    @Override
    public PageResultDTO<PostSummaryResponseDTO> search(SearchHomePostsUseCase.Query q) {
        PageRequest pageReq = PageRequest.of(q.page(), q.size());
        Page<Post> page = queryPostPort.searchHomeFeed(q.keyword(), q.sort(), pageReq);

        return buildPageResult(page, q.viewerId(), null);
    }

    @Override
    public PageResultDTO<PostSummaryResponseDTO> searchByAuthor(SearchAuthorPostsUseCase.Query q) {
        PageRequest pageReq = PageRequest.of(q.page(), q.size());
        Page<Post> page = queryPostPort.searchByAuthor(q.authorId(), q.keyword(), q.sort(), pageReq);

        // 작성자 피드이므로 fixedCommunityOrNull = null
        return buildPageResult(page, q.viewerId(), null);
    }
}
