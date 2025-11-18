package com.y11i.springcommddd.iam.domain.exception;


import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class MemberNotFound extends BaseException {
    public MemberNotFound(String message) {
        super(ErrorCode.MEMBER_NOT_FOUND, message);
    }
}
