package com.y11i.springcommddd.comments.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidCommentDepth extends BaseException {
    public InvalidCommentDepth(int value) {
        super(ErrorCode.COMMENT_DEPTH_INVALID, "depth must be >= 0, but was " + value);
    }
}
