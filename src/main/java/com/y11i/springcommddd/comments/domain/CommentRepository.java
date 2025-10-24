package com.y11i.springcommddd.comments.domain;

import com.y11i.springcommddd.posts.domain.PostId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * {@link Comment} 애그리게잇의 저장소 인터페이스.
 * <p>
 * 댓글(Comment) 도메인의 영속성과 조회를 담당하는 리포지토리로,
 * 게시글({@link PostId})과 부모 댓글({@link CommentId})을 기준으로
 * 계층 구조를 조회할 수 있는 기능을 제공합니다.
 * </p>
 *
 * <p><b>책임:</b></p>
 * <ul>
 *     <li>댓글의 저장 및 갱신</li>
 *     <li>고유 식별자({@link CommentId})를 통한 단건 조회</li>
 *     <li>특정 게시글의 루트 댓글(부모가 없는 댓글) 목록 조회</li>
 *     <li>특정 부모 댓글의 자식 댓글 목록 조회</li>
 *     <li>필요 시 게시글 전체 댓글 조회</li>
 * </ul>
 *
 * <p>
 * 실제 구현체는 인프라스트럭처 계층(예: JPA, MyBatis 등)에 위치합니다.
 * </p>
 *
 * @author y11
 */
public interface CommentRepository {

    /**
     * 댓글을 저장하거나 수정합니다.
     *
     * @param c 저장할 {@link Comment} 객체
     * @return 저장된 {@link Comment} 인스턴스
     */
    Comment save(Comment c);

    /**
     * 고유 식별자({@link CommentId})로 댓글을 조회합니다.
     *
     * @param id 조회할 댓글의 식별자
     * @return 존재하면 {@link Comment}를 포함하는 {@link Optional}, 없으면 비어 있음
     */
    Optional<Comment> findById(CommentId id);

    /**
     * 특정 게시글에 속한 루트 댓글(부모가 없는 댓글)을 페이징하여 조회합니다.
     *
     * @param postId   조회할 게시글의 식별자
     * @param pageable 페이지 및 정렬 정보
     * @return 루트 댓글의 {@link Page}
     */
    Page<Comment> findRootsByPostId(PostId postId, Pageable pageable);

    /**
     * 특정 부모 댓글에 대한 자식 댓글 목록을 조회합니다.
     * <p>
     * 구현체에 따라 시간순, 추천순 등의 정렬이 적용될 수 있습니다.
     * </p>
     *
     * @param parentId 부모 댓글의 식별자
     * @return 자식 댓글 목록 (없을 경우 빈 리스트)
     */
    List<Comment> findByParentId(CommentId parentId);

    /**
     * 특정 게시글의 모든 댓글을 계층 관계와 상관없이 조회합니다.
     * <p>
     * 주로 관리나 통계용으로 사용될 수 있습니다.
     * </p>
     *
     * @param postId   게시글의 식별자
     * @param pageable 페이지 및 정렬 정보
     * @return 게시글 전체 댓글의 {@link Page}
     */
    Page<Comment> findByPostId(PostId postId, Pageable pageable);
}
