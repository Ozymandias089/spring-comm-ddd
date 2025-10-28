package com.y11i.springcommddd.posts.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class PostStatusTransitionNotAllowed extends BaseException {
    public PostStatusTransitionNotAllowed(String detail) {
        super(ErrorCode.POST_STATUS_TRANSITION_FORBIDDEN, detail);
    }
}
