package com.y11i.springcommddd.posts.infrastructure;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.application.port.out.QueryPostPort;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostStatus;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

/**
 * {@link QueryPostPort} 의 JPA 기반 구현체.
 *
 * <p>
 * - 홈 피드<br>
 * - 특정 커뮤니티 피드<br>
 * 에 대한 읽기 쿼리를 담당한다.
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class PostQueryRepositoryAdapter implements QueryPostPort {

    private final JpaPostQueryRepository jpaPostQueryRepository;

    @Override
    public Page<Post> findHomeFeed(String sortKey, Pageable pageable) {
        PostStatus status = PostStatus.PUBLISHED;
        String normalized = normalizeSortKey(sortKey);

        return switch (normalized) {
            case "top" -> jpaPostQueryRepository.findHomeFeedOrderByTop(status, pageable);
            case "new" -> jpaPostQueryRepository.findHomeFeedOrderByNew(status, pageable);
            // TODO: "hot" 구현 시 분기 추가
            default -> jpaPostQueryRepository.findHomeFeedOrderByNew(status, pageable);
        };
    }

    @Override
    public Page<Post> findByCommunity(CommunityId communityId, String sortKey, Pageable pageable) {
        PostStatus status = PostStatus.PUBLISHED;
        String normalized = normalizeSortKey(sortKey);

        return switch (normalized) {
            case "top" -> jpaPostQueryRepository.findCommunityFeedOrderByTop(communityId, status, pageable);
            case "new" -> jpaPostQueryRepository.findCommunityFeedOrderByNew(communityId, status, pageable);
            // TODO: "hot" 구현 시 분기 추가
            default -> jpaPostQueryRepository.findCommunityFeedOrderByNew(communityId, status, pageable);
        };
    }

    @Override
    public Page<Post> findDraftsByAuthorId(MemberId authorId, String sortKey, Pageable pageable) {
        PostStatus status = PostStatus.DRAFT;
        String normalized = normalizeSortKey(sortKey);
        return jpaPostQueryRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, status, pageable);
    }

    @Override
    public Page<Post> searchHomeFeed(String keyword, String sortKey, Pageable pageable) {
        PostStatus status = PostStatus.PUBLISHED;
        String normalized = normalizeSortKey(sortKey);
        String like = "%" + keyword + "%";

        return switch (normalized) {
            case "top" ->
                    jpaPostQueryRepository.searchHomeFeedOrderByTop(status, like, pageable);
            case "new" ->
                    jpaPostQueryRepository.searchHomeFeedOrderByNew(status, like, pageable);
            default ->
                    jpaPostQueryRepository.searchHomeFeedOrderByNew(status, like, pageable);
        };
    }

    @Override
    public Page<Post> searchByCommunity(CommunityId communityId, String keyword, String sortKey, Pageable pageable) {
        PostStatus status = PostStatus.PUBLISHED;
        String normalized = normalizeSortKey(sortKey);
        String like = "%" + keyword + "%";

        return switch (normalized) {
            case "top" ->
                    jpaPostQueryRepository.searchCommunityFeedOrderByTop(communityId, status, like, pageable);
            case "new" ->
                    jpaPostQueryRepository.searchCommunityFeedOrderByNew(communityId, status, like, pageable);
            default ->
                    jpaPostQueryRepository.searchCommunityFeedOrderByNew(communityId, status, like, pageable);
        };
    }

    @Override
    public Page<Post> searchByAuthor(MemberId authorId, @Nullable String keyword, String sortKey, Pageable pageable) {
        PostStatus status = PostStatus.PUBLISHED;
        String normalized = normalizeSortKey(sortKey);

        // 키워드 없으면 "그 유저의 전체 게시글 피드" 느낌으로
        if (keyword == null || keyword.isBlank()) {
            return switch (normalized) {
                case "top" ->
                        jpaPostQueryRepository.findAuthorFeedOrderByTop(authorId, status, pageable);
                case "new" ->
                        jpaPostQueryRepository.findAuthorFeedOrderByNew(authorId, status, pageable);
                default ->
                        jpaPostQueryRepository.findAuthorFeedOrderByNew(authorId, status, pageable);
            };
        }

        // 키워드 있으면 like 검색
        String like = "%" + keyword + "%";

        return switch (normalized) {
            case "top" ->
                    jpaPostQueryRepository.searchAuthorFeedOrderByTop(authorId, status, like, pageable);
            case "new" ->
                    jpaPostQueryRepository.searchAuthorFeedOrderByNew(authorId, status, like, pageable);
            default ->
                    jpaPostQueryRepository.searchAuthorFeedOrderByNew(authorId, status, like, pageable);
        };
    }

    /**
     * 정렬 키를 소문자로 정규화하고, null이면 "new"로 처리한다.
     */
    private String normalizeSortKey(String sortKey) {
        if (sortKey == null || sortKey.isBlank()) return "new";
        return sortKey.toLowerCase();
    }
}