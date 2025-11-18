package com.y11i.springcommddd.shared.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidIdentifierFormat extends BaseException {
    public InvalidIdentifierFormat(String message) {
        super(ErrorCode.ID_FORMAT_INVALID, message);
    }
}
