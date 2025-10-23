package com.y11i.springcommddd.votes.domain;

import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.Optional;

public interface CommentVoteRepository {
    CommentVote save(CommentVote v);
    Optional<CommentVote> findByCommentIdAndVoterId(CommentId commentId, MemberId voterId);
    void delete(CommentVote v);
}
