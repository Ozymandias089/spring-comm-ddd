package com.y11i.springcommddd.comments.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class CommentStatusTransitionNotAllowed extends BaseException {
    public CommentStatusTransitionNotAllowed(String message) {
        super(ErrorCode.COMMENT_STATUS_TRANSITION_FORBIDDEN, message);
    }
}
