package com.y11i.springcommddd.votes.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidVoteValue extends BaseException {
    public InvalidVoteValue(String message) {
        super(ErrorCode.VOTE_VALUE_INVALID, message);
    }
}
