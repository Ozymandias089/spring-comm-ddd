package com.y11i.springcommddd.votes.application.service;

import com.y11i.springcommddd.comments.application.port.out.LoadCommentPort;
import com.y11i.springcommddd.comments.application.port.out.SaveCommentPort;
import com.y11i.springcommddd.comments.domain.Comment;
import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.comments.domain.exception.CommentNotFound;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.votes.application.port.in.VoteCommentUseCase;
import com.y11i.springcommddd.votes.domain.CommentVote;
import com.y11i.springcommddd.votes.domain.CommentVoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VoteCommentService implements VoteCommentUseCase {
    private final CommentVoteRepository commentVoteRepository;
    private final LoadCommentPort loadCommentPort;
    private final SaveCommentPort saveCommentPort;

    // ----------------------------------------------------------------------
    // Public API
    // ----------------------------------------------------------------------

    /**
     * 댓글에 추천(+1)을 표시한다.
     *
     * <p>기존 상태에 따른 결과</p>
     * <ul>
     *   <li>없음 → +1 생성</li>
     *   <li>-1 → +1 로 변경</li>
     *   <li>+1 → 취소(0)</li>
     * </ul>
     *  @param commentId 댓글 ID
     *
     * @param voterId 투표자 ID
     */
    @Override
    @Transactional
    public void upvote(CommentId commentId, MemberId voterId) {
        applyVote(commentId, voterId, +1);
    }

    /**
     * 댓글에 비추천(-1)을 표시한다.
     *
     * <p>기존 상태에 따른 결과</p>
     * <ul>
     *   <li>없음 → -1 생성</li>
     *   <li>+1 → -1로 변경</li>
     *   <li>-1 → 취소(0)</li>
     * </ul>
     *  @param commentId 댓글 ID
     *
     * @param voterId 투표자 ID
     */
    @Override
    @Transactional
    public void downvote(CommentId commentId, MemberId voterId) {
        applyVote(commentId, voterId, -1);
    }

    /**
     * 댓글에 대한 나의 투표를 취소한다.
     *
     * <p>
     * 존재하면 삭제되고, 없으면 아무 동작도 하지 않는다.
     * </p>
     *
     * @param commentId 댓글 ID
     * @param voterId   투표자 ID
     */
    @Override
    @Transactional
    public void cancelVote(CommentId commentId, MemberId voterId) {
        applyVote(commentId, voterId, 0);
    }

    // ----------------------------------------------------------------------
    // 내부 헬퍼
    // ----------------------------------------------------------------------
    private void applyVote(CommentId commentId, MemberId voterId, int desired) {
        if (desired != -1 && desired != 0 && desired != 1) {
            log.error("desired vote must be -1, 0, or 1");
            throw new IllegalArgumentException("desired vote must be -1, 0, or 1");
        }

        // 1. 댓글 로드
        Comment comment = loadCommentPort.loadById(commentId).orElseThrow(() -> new CommentNotFound("Comment not found"));
        // 2. comment가 visible이어야만 투표가능
        comment.ensureNotDeleted("You cannot vote on deleted comment");
        // 3. 기존 투표 조회
        Optional<CommentVote> existingOpt = commentVoteRepository.findByCommentIdAndVoterId(commentId, voterId);
        int oldValue = existingOpt.map(CommentVote::value).orElse(0);

        // 4. 새 값 결정(toggle)
        int newValue = decideNewValue(oldValue, desired);

        // CommentVote agg 처리
        if(oldValue == 0 && newValue != 0) {
            // 새 투표 생성
            CommentVote vote = CommentVote.cast(commentId, voterId, newValue);
            commentVoteRepository.save(vote);
        } else if (oldValue != 0 && newValue == 0) {
            // 투표 취소(삭제)
            existingOpt.ifPresent(commentVoteRepository::delete);
        } else if (oldValue != 0 && newValue != 0) {
            // 방향 변경
            CommentVote existing = existingOpt.orElseThrow();
            existing.setValue(newValue);
            commentVoteRepository.save(existing);
        }

        // 게시글 집계값 갱신
        comment.applyVoteDelta(oldValue, newValue);

        saveCommentPort.save(comment);
    }

    private int decideNewValue(int oldValue, int desired) {
        if (desired == 0) {
            return 0; // 무조건 취소
        }

        if (desired == 1) {
            // upvote 요청
            return (oldValue == 1) ? 0 : 1;
        } else { // desired == -1
            // downvote 요청
            return (oldValue == -1) ? 0 : -1;
        }
    }
}
