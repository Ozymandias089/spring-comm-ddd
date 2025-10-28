package com.y11i.springcommddd.votes.domain;

import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.shared.domain.ValueObject;
import com.y11i.springcommddd.votes.domain.exception.InvalidVoteValue;

/** 댓글 단위 내 투표값 */
public record MyCommentVote(CommentId id, int value) implements ValueObject, MyVoteValue {
    public MyCommentVote {
        if (value != -1 && value != 1) throw new InvalidVoteValue("value must be -1 or 1");
    }
}