package com.y11i.springcommddd.posts.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class PostNotVotable extends BaseException {
    public PostNotVotable(String message) {
        super(ErrorCode.POST_VOTE_UNAVAILABLE, message);
    }
}
