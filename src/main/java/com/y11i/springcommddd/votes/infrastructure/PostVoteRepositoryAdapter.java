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

    private final PostVoteRepository postVoteRepository;
    public PostVoteRepositoryAdapter(PostVoteRepository postVoteRepository) {
        this.postVoteRepository = postVoteRepository;
    }

    /**
     * @param v A vote for a post
     * @return saved vote
     */
    @Override @Transactional
    public PostVote save(PostVote v) {
        return postVoteRepository.save(v);
    }

    /**
     * @param postId Id of a Post to vote
     * @param voterId Id of a voter to cast
     * @return Returns All PostVote Objects matching two parameters provided
     */
    @Override
    public Optional<PostVote> findByPostIdAndVoterId(PostId postId, MemberId voterId) {
        return postVoteRepository.findByPostIdAndVoterId(postId, voterId);
    }

    /**
     * @param v A vote to delete
     */
    @Override @Transactional
    public void delete(PostVote v) {
        postVoteRepository.delete(v);
    }
}
