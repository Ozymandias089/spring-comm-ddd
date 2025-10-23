package com.y11i.springcommddd.votes.infrastructure;

import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.votes.domain.CommentVote;
import com.y11i.springcommddd.votes.domain.CommentVoteRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class CommentVoteRepositoryAdapter implements CommentVoteRepository {
    private final CommentVoteRepository commentVoteRepository;
    public CommentVoteRepositoryAdapter(CommentVoteRepository commentVoteRepository) {
        this.commentVoteRepository = commentVoteRepository;
    }

    /**
     * @param v A vote to cast
     * @return saved vote
     */
    @Override @Transactional
    public CommentVote save(CommentVote v) {
        return commentVoteRepository.save(v);
    }

    /**
     * @param commentId Comment id parameter
     * @param voterId voter id parameter
     * @return Find all votes cast matching commentId and voterId
     */
    @Override
    public Optional<CommentVote> findByCommentIdAndVoterId(CommentId commentId, MemberId voterId) {
        return commentVoteRepository.findByCommentIdAndVoterId(commentId, voterId);
    }

    /**
     * @param v A vote to delete
     */
    @Override @Transactional
    public void delete(CommentVote v) {
        commentVoteRepository.delete(v);
    }
}
