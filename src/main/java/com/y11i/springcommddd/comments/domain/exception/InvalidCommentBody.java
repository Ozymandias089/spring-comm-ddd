package com.y11i.springcommddd.comments.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidCommentBody extends BaseException {
    public InvalidCommentBody(String reason) { super(ErrorCode.COMMENT_BODY_INVALID, reason); }
}
