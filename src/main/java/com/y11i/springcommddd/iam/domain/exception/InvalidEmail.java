package com.y11i.springcommddd.iam.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidEmail extends BaseException {
    public InvalidEmail(String message) {
        super(ErrorCode.EMAIL_INVALID, message);
    }
}
