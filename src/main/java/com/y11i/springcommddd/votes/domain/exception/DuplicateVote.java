package com.y11i.springcommddd.votes.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class DuplicateVote extends BaseException {
    public DuplicateVote(String message) {
        super(ErrorCode.VOTE_DUPLICATE, message);
    }
}
