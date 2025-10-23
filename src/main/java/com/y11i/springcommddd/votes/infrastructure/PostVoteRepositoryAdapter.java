package com.y11i.springcommddd.votes.infrastructure;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.votes.domain.PostVote;
import com.y11i.springcommddd.votes.domain.PostVoteRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class PostVoteRepositoryAdapter implements PostVoteRepository {

    private final JpaPostVoteRepository jpaPostVoteRepository;
    public PostVoteRepositoryAdapter(JpaPostVoteRepository jpaPostVoteRepository) {
        this.jpaPostVoteRepository = jpaPostVoteRepository;
    }

    /**
     * @param v A vote for a post
     * @return saved vote
     */
    @Override @Transactional
    public PostVote save(PostVote v) {
        return jpaPostVoteRepository.save(v);
    }

    /**
     * @param postId Id of a Post to vote
     * @param voterId Id of a voter to cast
     * @return Returns All PostVote Objects matching two parameters provided
     */
    @Override
    public Optional<PostVote> findByPostIdAndVoterId(PostId postId, MemberId voterId) {
        return jpaPostVoteRepository.findByPostIdAndVoterId(postId, voterId);
    }

    /**
     * @param v A vote to delete
     */
    @Override @Transactional
    public void delete(PostVote v) {
        jpaPostVoteRepository.delete(v);
    }
}
