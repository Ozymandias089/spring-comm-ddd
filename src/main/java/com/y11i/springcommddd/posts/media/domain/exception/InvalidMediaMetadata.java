package com.y11i.springcommddd.posts.media.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidMediaMetadata extends BaseException {
    public InvalidMediaMetadata(String detail) {
        super(ErrorCode.MEDIA_METADATA_INVALID, detail);
    }
}
