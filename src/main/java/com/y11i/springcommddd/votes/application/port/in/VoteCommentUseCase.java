package com.y11i.springcommddd.votes.application.port.in;

import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.iam.domain.MemberId;

/**
 * 댓글에 대한 추천/비추천(투표) 유스케이스.
 *
 * <p>
 * 한 회원(MemberId)은 하나의 댓글(CommentId)에 대해 최대 하나의 투표만 가질 수 있으며,
 * 추천(+1), 비추천(-1), 취소(0) 세 가지 상태를 가진다.
 * </p>
 *
 * <p>
 * 이 유스케이스는 투표 애그리게잇(CommentVote)과
 * 댓글 애그리게잇(Comment)의 투표 집계(up/down count)를 함께 관리한다.
 * </p>
 */
public interface VoteCommentUseCase {
    /**
     * 댓글에 추천(+1)을 표시한다.
     *
     * <p>기존 상태에 따른 결과</p>
     * <ul>
     *   <li>없음 → +1 생성</li>
     *   <li>-1 → +1 로 변경</li>
     *   <li>+1 → 취소(0)</li>
     * </ul>
     *
     * @param commentId 댓글 ID
     * @param voterId 투표자 ID
     */
    void upvote(CommentId commentId, MemberId voterId);

    /**
     * 댓글에 비추천(-1)을 표시한다.
     *
     * <p>기존 상태에 따른 결과</p>
     * <ul>
     *   <li>없음 → -1 생성</li>
     *   <li>+1 → -1로 변경</li>
     *   <li>-1 → 취소(0)</li>
     * </ul>
     *
     * @param commentId 댓글 ID
     * @param voterId 투표자 ID
     */
    void downvote(CommentId commentId, MemberId voterId);

    /**
     * 댓글에 대한 나의 투표를 취소한다.
     *
     * <p>
     * 존재하면 삭제되고, 없으면 아무 동작도 하지 않는다.
     * </p>
     *
     * @param commentId  댓글 ID
     * @param voterId 투표자 ID
     */

    void cancelVote(CommentId commentId, MemberId voterId);
}
