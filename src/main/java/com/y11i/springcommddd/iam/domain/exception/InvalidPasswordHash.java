package com.y11i.springcommddd.iam.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidPasswordHash extends BaseException {
    public InvalidPasswordHash(String message) {
        super(ErrorCode.PASSWORD_HASH_INVALID, message);
    }
}
