package com.y11i.springcommddd.comments.infrastructure;

import com.y11i.springcommddd.comments.domain.Comment;
import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.posts.domain.PostId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA 기반의 댓글 리포지토리.
 * <p>
 * {@link Comment} 엔티티를 데이터베이스에 영속화하고,
 * 게시글({@link PostId}) 및 부모 댓글({@link CommentId})을 기준으로
 * 다양한 조회 기능을 제공합니다.
 * </p>
 *
 * <p><b>역할:</b></p>
 * <ul>
 *     <li>Spring Data JPA가 자동으로 구현체를 생성함</li>
 *     <li>메서드 이름 기반 쿼리 파생(query derivation)을 통해 댓글 계층 구조 조회</li>
 *     <li>{@link CommentRepositoryAdapter}에서 주입받아 사용됨</li>
 * </ul>
 *
 * @author y11
 */
public interface JpaCommentRepository extends JpaRepository<Comment, CommentId> {

    /**
     * 특정 게시글의 루트 댓글(부모가 없는 댓글)을 작성일 순으로 페이징 조회합니다.
     *
     * @param postId   게시글 식별자
     * @param pageable 페이지 및 정렬 정보
     * @return 루트 댓글 목록을 포함한 {@link Page}
     */
    Page<Comment> findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(PostId postId, Pageable pageable);

    /**
     * 특정 부모 댓글의 자식 댓글을 작성일 순으로 조회합니다.
     *
     * @param parentId 부모 댓글의 식별자
     * @return 자식 댓글 목록 (없을 경우 빈 리스트)
     */
    List<Comment> findByParentIdOrderByCreatedAtAsc(CommentId parentId);

    /**
     * 특정 게시글의 모든 댓글을 페이징하여 조회합니다.
     * <p>
     * 계층 구조와 상관없이 전체 댓글을 반환합니다.
     * </p>
     *
     * @param postId   게시글 식별자
     * @param pageable 페이지 및 정렬 정보
     * @return 게시글 전체 댓글을 포함한 {@link Page}
     */
    Page<Comment> findByPostId(PostId postId, Pageable pageable); // 선택

    long countByPostId(PostId postId);
}
