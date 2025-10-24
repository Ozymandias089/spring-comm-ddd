package com.y11i.springcommddd.votes.domain;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;

import java.util.Optional;

/**
 * {@link PostVote} 애그리게잇의 저장소 인터페이스.
 * <p>
 * 게시글 투표(PostVote) 도메인의 영속성과 조회를 담당하며,
 * 게시글({@link PostId})과 투표자({@link MemberId})를 기준으로 단일 투표를 조회하거나 삭제할 수 있습니다.
 * </p>
 *
 * <p><b>책임:</b></p>
 * <ul>
 *     <li>투표 정보의 생성 및 수정</li>
 *     <li>게시글과 투표자 ID로 투표 단건 조회</li>
 *     <li>투표 삭제(취소)</li>
 * </ul>
 *
 * <p>
 * 실제 구현은 인프라스트럭처 계층의 {@code PostVoteRepositoryAdapter}에서 제공합니다.
 * </p>
 *
 * @author y11
 */
public interface PostVoteRepository {

    /**
     * 게시글 투표를 저장하거나 수정합니다.
     *
     * @param v 저장할 {@link PostVote} 객체
     * @return 저장된 {@link PostVote} 인스턴스
     */
    PostVote save(PostVote v);

    /**
     * 게시글({@link PostId})과 투표자({@link MemberId})를 기준으로 투표를 조회합니다.
     *
     * @param postId   게시글 식별자
     * @param voterId  투표자 식별자
     * @return 일치하는 {@link PostVote}가 존재하면 반환, 없으면 비어 있음
     */
    Optional<PostVote> findByPostIdAndVoterId(PostId postId, MemberId voterId);

    /**
     * 투표 정보를 삭제합니다.
     *
     * @param v 삭제할 {@link PostVote} 객체
     */
    void delete(PostVote v);
}
