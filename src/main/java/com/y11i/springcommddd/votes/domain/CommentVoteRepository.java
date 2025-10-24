package com.y11i.springcommddd.votes.domain;

import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.Optional;

/**
 * {@link CommentVote} 애그리게잇의 저장소 인터페이스.
 * <p>
 * 댓글 투표(CommentVote) 도메인의 영속성과 조회를 담당하며,
 * 댓글({@link CommentId})과 투표자({@link MemberId})를 기준으로
 * 단일 투표를 저장/조회/삭제합니다.
 * </p>
 *
 * <p><b>책임:</b></p>
 * <ul>
 *     <li>투표 정보의 생성 및 수정</li>
 *     <li>댓글 ID와 투표자 ID로 단건 조회</li>
 *     <li>투표 삭제(취소)</li>
 * </ul>
 *
 * <p>
 * 실제 구현은 인프라스트럭처 계층의 어댑터에서 제공합니다.
 * </p>
 */
public interface CommentVoteRepository {

    /**
     * 댓글 투표를 저장하거나 수정합니다.
     *
     * @param v 저장할 {@link CommentVote} 객체
     * @return 저장된 {@link CommentVote} 인스턴스
     */
    CommentVote save(CommentVote v);

    /**
     * 댓글({@link CommentId})과 투표자({@link MemberId})를 기준으로 투표를 조회합니다.
     *
     * @param commentId 댓글 식별자
     * @param voterId   투표자 식별자
     * @return 일치하는 {@link CommentVote}가 존재하면 반환, 없으면 비어 있음
     */
    Optional<CommentVote> findByCommentIdAndVoterId(CommentId commentId, MemberId voterId);

    /**
     * 투표 정보를 삭제합니다.
     *
     * @param v 삭제할 {@link CommentVote} 객체
     */
    void delete(CommentVote v);
}
