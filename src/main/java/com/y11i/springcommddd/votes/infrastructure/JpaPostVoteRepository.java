package com.y11i.springcommddd.votes.infrastructure;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.votes.domain.PostVote;
import com.y11i.springcommddd.votes.domain.PostVoteId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaPostVoteRepository extends JpaRepository<PostVote, PostVoteId> {
    Optional<PostVote> findByPostIdAndVoterId(PostId postId, MemberId voterId);
}
