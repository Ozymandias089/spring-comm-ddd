package com.y11i.springcommddd.votes.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class VoteNotFound extends BaseException {
    public VoteNotFound(String message) {
        super(ErrorCode.VOTE_NOT_FOUND, message);
    }
}
