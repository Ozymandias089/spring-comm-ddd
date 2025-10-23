package com.y11i.springcommddd.votes.domain;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;

import java.util.Optional;

public interface PostVoteRepository {
    PostVote save(PostVote v);
    Optional<PostVote> findByPostIdAndVoterId(PostId postId, MemberId voterId);
    void delete(PostVote v);
}
