package com.y11i.springcommddd.posts.media.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidUrl extends BaseException {
    public InvalidUrl(String reason) {
        super(ErrorCode.MEDIA_URL_INVALID, reason);
    }
}
