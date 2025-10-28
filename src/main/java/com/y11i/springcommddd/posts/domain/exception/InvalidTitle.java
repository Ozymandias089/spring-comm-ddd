package com.y11i.springcommddd.posts.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidTitle extends BaseException {
    public InvalidTitle(String reason) { super(ErrorCode.POST_TITLE_INVALID, reason); }
}
