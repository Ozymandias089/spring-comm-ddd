package com.y11i.springcommddd.posts.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidContent extends BaseException {
    public InvalidContent(String reason) { super(ErrorCode.POST_CONTENT_INVALID, reason); }
}
