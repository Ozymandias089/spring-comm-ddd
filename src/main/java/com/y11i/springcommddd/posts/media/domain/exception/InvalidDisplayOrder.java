package com.y11i.springcommddd.posts.media.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidDisplayOrder extends BaseException {
    public InvalidDisplayOrder(int value) {
        super(ErrorCode.MEDIA_DISPLAY_ORDER_INVALID, "displayOrder must be >= 0, but was " + value);
    }
}
