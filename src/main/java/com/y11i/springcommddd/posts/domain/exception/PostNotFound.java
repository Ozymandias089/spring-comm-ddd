package com.y11i.springcommddd.posts.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class PostNotFound extends BaseException {
    public PostNotFound(String message) { super(ErrorCode.POST_NOT_FOUND, message); }
}
