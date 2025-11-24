package com.y11i.springcommddd.iam.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class UnauthorizedMemberAction extends BaseException {
    public UnauthorizedMemberAction(String message) {
        super(ErrorCode.MEMBER_ACTION_UNAUTHORIZED, message);
    }
}
