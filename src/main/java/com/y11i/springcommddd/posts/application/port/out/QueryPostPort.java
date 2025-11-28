package com.y11i.springcommddd.posts.application.port.out;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.Post;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 게시글 리스트 조회용 쿼리 포트.
 *
 * <p>
 * - 홈 피드 (커뮤니티 전체 대상)<br>
 * - 특정 커뮤니티 내 게시글 목록
 * </p>
 *
 * 실제 정렬("new", "top", "hot" 등)에 대한 구현은 인프라 계층
 * (예: JPA, QueryDSL)에서 담당한다.
 */
public interface QueryPostPort {

    /**
     * 홈 피드용 게시글 페이지를 조회한다.
     *
     * @param sortKey "new", "top", "hot" 등
     * @param pageable 페이지 정보
     */
    Page<Post> findHomeFeed(String sortKey, Pageable pageable);

    /**
     * 특정 커뮤니티 내 게시글 페이지를 조회한다.
     *
     * @param communityId 커뮤니티 ID
     * @param sortKey     정렬 키
     * @param pageable    페이지 정보
     */
    Page<Post> findByCommunity(CommunityId communityId, String sortKey, Pageable pageable);

    Page<Post> findDraftsByAuthorId(MemberId authorId, String sortKey, Pageable pageable);

    // --- 검색용 ---
    Page<Post> searchHomeFeed(String keyword, String sortKey, Pageable pageable);

    Page<Post> searchByCommunity(CommunityId communityId, String keyword, String sortKey, Pageable pageable);

    Page<Post> searchByAuthor(MemberId authorId, @Nullable String keyword, String sortKey, Pageable pageable);
}
