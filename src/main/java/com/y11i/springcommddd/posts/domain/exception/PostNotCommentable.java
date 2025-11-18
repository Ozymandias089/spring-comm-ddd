package com.y11i.springcommddd.posts.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class PostNotCommentable extends BaseException {
    public PostNotCommentable(String message) {
        super(ErrorCode.COMMENT_UNAVAILABLE, message);
    }
}
