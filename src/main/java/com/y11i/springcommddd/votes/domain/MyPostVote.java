package com.y11i.springcommddd.votes.domain;

import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.shared.domain.ValueObject;
import com.y11i.springcommddd.votes.domain.exception.InvalidVoteValue;

/** 게시글 단위의 내 투표값 */
public record MyPostVote(PostId id, int value) implements ValueObject, MyVoteValue {
    public MyPostVote {
        if (value != -1 && value != 1) throw new InvalidVoteValue("value must be -1 or 1");
    }
}
