package com.y11i.springcommddd.common.exception;

import com.y11i.springcommddd.common.api.ErrorCode;

public class InvalidIdentifierFormatException extends BaseException {
    public InvalidIdentifierFormatException(String message) {
        super(ErrorCode.ID_FORMAT_INVALID, message);
    }
}
