package com.y11i.springcommddd.votes.infrastructure;


import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.votes.domain.CommentVote;
import com.y11i.springcommddd.votes.domain.CommentVoteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaCommentVoteRepository extends JpaRepository<CommentVote, CommentId> {
    Optional<CommentVote>  findByCommentIdAndVoterId(CommentId commentId, MemberId voterId);
}
