package com.y11i.springcommddd.posts.infrastructure;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 게시글 피드/리스트 조회용 JPA 리포지토리.
 *
 * <p>
 * - 홈 피드<br>
 * - 특정 커뮤니티 내 피드
 * </p>
 */
public interface JpaPostQueryRepository extends JpaRepository<Post, PostId> {

    // -------------------- 홈 피드 --------------------

    @Query("""
           select p
           from Post p
           where p.status = :status
           order by p.publishedAt desc
           """)
    Page<Post> findHomeFeedOrderByNew(@Param("status") PostStatus status, Pageable pageable);

    @Query("""
           select p
           from Post p
           where p.status = :status
           order by (p.upCount - p.downCount) desc, p.publishedAt desc
           """)
    Page<Post> findHomeFeedOrderByTop(@Param("status") PostStatus status, Pageable pageable);

    // TODO: hot 정렬 알고리즘(시간 가중치 포함) 필요 시 추가
    // @Query(" ... ")
    // Page<Post> findHomeFeedOrderByHot(...);


    // -------------------- 특정 커뮤니티 피드 --------------------

    @Query("""
           select p
           from Post p
           where p.status = :status
             and p.communityId = :communityId
           order by p.publishedAt desc
           """)
    Page<Post> findCommunityFeedOrderByNew(
            @Param("communityId") CommunityId communityId,
            @Param("status") PostStatus status,
            Pageable pageable
    );

    @Query("""
           select p
           from Post p
           where p.status = :status
             and p.communityId = :communityId
           order by (p.upCount - p.downCount) desc, p.publishedAt desc
           """)
    Page<Post> findCommunityFeedOrderByTop(
            @Param("communityId") CommunityId communityId,
            @Param("status") PostStatus status,
            Pageable pageable
    );

    @Query("""
        select p
        from Post p
        where p.status = :status
        and p.authorId = :authorId
        order by p.createdAt desc
        """)
    Page<Post> findByAuthorIdOrderByCreatedAtDesc(
            @Param("authorId")MemberId authorId,
            @Param("status") PostStatus status,
            Pageable pageable
            );
    // =====================================
    //             홈 검색
    // =====================================

    @Query("""
           select p
           from Post p
           where p.status = :status
             and (
                    p.title.value   like :keyword
                 or p.content.value like :keyword
                 )
           order by p.createdAt desc
           """)
    Page<Post> searchHomeFeedOrderByNew(
            @Param("status") PostStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
           select p
           from Post p
           where p.status = :status
             and (
                    p.title.value   like :keyword
                 or p.content.value like :keyword
                 )
           order by (p.upCount - p.downCount) desc, p.publishedAt desc
           """)
    Page<Post> searchHomeFeedOrderByTop(
            @Param("status") PostStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // =====================================
    //           커뮤니티 검색
    // =====================================

    @Query("""
           select p
           from Post p
           where p.status = :status
             and p.communityId = :communityId
             and (
                    p.title.value   like :keyword
                 or p.content.value like :keyword
                 )
           order by p.createdAt desc
           """)
    Page<Post> searchCommunityFeedOrderByNew(
            @Param("communityId") CommunityId communityId,
            @Param("status") PostStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    @Query("""
           select p
           from Post p
           where p.status = :status
             and p.communityId = :communityId
             and (
                    p.title.value   like :keyword
                 or p.content.value like :keyword
                 )
           order by (p.upCount - p.downCount) desc, p.publishedAt desc
           """)
    Page<Post> searchCommunityFeedOrderByTop(
            @Param("communityId") CommunityId communityId,
            @Param("status") PostStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}