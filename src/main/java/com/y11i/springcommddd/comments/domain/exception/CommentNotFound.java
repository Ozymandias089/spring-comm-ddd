package com.y11i.springcommddd.comments.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class CommentNotFound extends BaseException {
    public CommentNotFound(String message) {
        super(ErrorCode.COMMENT_NOT_FOUND, message);
    }
}
