package com.y11i.springcommddd.posts.infrastructure;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA 기반의 게시글 리포지토리.
 * <p>
 * {@link Post} 엔티티를 데이터베이스에 영속화하고,
 * 작성자({@link MemberId})나 커뮤니티({@link CommunityId})를 기준으로
 * 게시글을 조회하는 기능을 제공합니다.
 * </p>
 *
 * <p><b>역할:</b></p>
 * <ul>
 *     <li>Spring Data JPA가 자동으로 구현체를 생성</li>
 *     <li>쿼리 메서드 파생(query derivation)을 통한 조회 지원</li>
 *     <li>{@link PostRepositoryAdapter}에서 주입받아 사용됨</li>
 * </ul>
 *
 * @see JpaRepository
 * @see PostRepositoryAdapter
 * @see Post
 * @see MemberId
 * @see CommunityId
 */
@Repository
public interface JpaPostRepository extends JpaRepository<Post, PostId> {

    /**
     * 작성자({@link MemberId})의 모든 게시글을 조회합니다.
     *
     * @param authorId 작성자 식별자
     * @return 해당 작성자의 {@link Post} 목록
     */
    List<Post> findByAuthorId(MemberId authorId);

    /**
     * 작성자({@link MemberId})의 게시글을 페이지 단위로 조회합니다.
     *
     * @param authorId 작성자 식별자
     * @param pageable 페이지 및 정렬 정보
     * @return 해당 작성자의 {@link Page} 형태 게시글 목록
     */
    Page<Post> findByAuthorId(MemberId authorId, Pageable pageable);

    /**
     * 커뮤니티({@link CommunityId})의 모든 게시글을 조회합니다.
     *
     * @param communityId 커뮤니티 식별자
     * @return 커뮤니티의 모든 {@link Post} 목록
     */
    List<Post> findByCommunityId(CommunityId communityId);

    /**
     * 커뮤니티({@link CommunityId})의 게시글을 페이지 단위로 조회합니다.
     *
     * @param communityId 커뮤니티 식별자
     * @param pageable 페이지 및 정렬 정보
     * @return 커뮤니티의 {@link Page} 형태 게시글 목록
     */
    Page<Post> findByCommunityId(CommunityId communityId, Pageable pageable);

}
