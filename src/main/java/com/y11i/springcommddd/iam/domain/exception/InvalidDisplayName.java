package com.y11i.springcommddd.iam.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidDisplayName extends BaseException {
    public InvalidDisplayName(String message) {
        super(ErrorCode.DISPLAY_NAME_INVALID, message);
    }
}
