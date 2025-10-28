package com.y11i.springcommddd.shared.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidImageUrl extends BaseException {
    public InvalidImageUrl(String message) {
        super(ErrorCode.IMAGE_URL_INVALID, message);
    }
}
