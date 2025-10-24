package com.y11i.springcommddd.posts.domain;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * {@link Post} 애그리게잇의 저장소 인터페이스.
 * <p>
 * 게시글(Post) 도메인의 영속성과 조회를 담당하며,
 * 작성자({@link MemberId}) 및 커뮤니티({@link CommunityId})를 기준으로
 * 다양한 형태의 조회 기능을 제공합니다.
 * </p>
 *
 * <p><b>책임:</b></p>
 * <ul>
 *     <li>게시글의 생성 및 수정</li>
 *     <li>식별자({@link PostId})를 통한 단건 조회</li>
 *     <li>작성자 ID 또는 커뮤니티 ID를 통한 게시글 목록 조회</li>
 *     <li>페이지네이션 기반 조회 기능 제공</li>
 *     <li>하드 삭제(영구 삭제) 지원</li>
 * </ul>
 *
 * <p>
 * 도메인 계층은 이 인터페이스에만 의존하며,
 * 실제 구현체는 인프라스트럭처 계층에 존재합니다.
 * </p>
 *
 * @author y11
 */
public interface PostRepository {

    /**
     * 게시글을 저장하거나 수정합니다.
     *
     * @param post 저장할 {@link Post} 객체
     * @return 저장된 {@link Post} 인스턴스
     */
    Post save(Post post);

    /**
     * 게시글 식별자({@link PostId})로 단건 조회합니다.
     *
     * @param id 게시글 식별자
     * @return 존재하면 {@link Post}, 없으면 비어 있음
     */
    Optional<Post> findById(PostId id);

    /**
     * 모든 게시글을 조회합니다.
     *
     * @return 전체 {@link Post} 목록
     */
    List<Post> findAll();

    /**
     * 특정 작성자({@link MemberId})의 게시글을 모두 조회합니다.
     *
     * @param authorId 작성자의 식별자
     * @return 해당 작성자가 작성한 모든 {@link Post} 목록
     */
    List<Post> findByAuthorId(MemberId authorId);

    /**
     * 특정 작성자({@link MemberId})의 게시글을 페이징하여 조회합니다.
     *
     * @param authorId 작성자 식별자
     * @param pageable 페이지 및 정렬 정보
     * @return 작성자 기준 {@link Page} 형태의 게시글 목록
     */
    Page<Post> findByAuthorId(MemberId authorId, Pageable pageable);

    /**
     * 게시글을 영구적으로 삭제합니다.
     * <p>
     * 일반적인 “삭제” 유즈케이스에서는 상태 변경(soft delete)을 권장합니다.
     * </p>
     *
     * @param post 삭제할 {@link Post} 객체
     */
    void delete(Post post);

    /**
     * 특정 커뮤니티({@link CommunityId})의 모든 게시글을 조회합니다.
     *
     * @param communityId 커뮤니티 식별자
     * @return 해당 커뮤니티에 속한 모든 {@link Post} 목록
     */
    List<Post> findByCommunityId(CommunityId communityId);

    /**
     * 특정 커뮤니티({@link CommunityId})의 게시글을 페이징하여 조회합니다.
     *
     * @param communityId 커뮤니티 식별자
     * @param pageable 페이지 및 정렬 정보
     * @return 커뮤니티 기준 {@link Page} 형태의 게시글 목록
     */
    Page<Post> findByCommunityId(CommunityId communityId, Pageable pageable);
}
