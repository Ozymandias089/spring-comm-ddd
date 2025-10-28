package com.y11i.springcommddd.iam.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class MemberStatusTransitionNotAllowed extends BaseException {
    public MemberStatusTransitionNotAllowed(String message) {
        super(ErrorCode.MEMBER_STATUS_TRANSITION_FORBIDDEN, message);
    }
}
