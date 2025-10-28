package com.y11i.springcommddd.iam.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class MemberDeletedModificationNotAllowed extends BaseException {
    public MemberDeletedModificationNotAllowed(String message) {
        super(ErrorCode.MEMBER_DELETED_MODIFICATION_FORBIDDEN, message);
    }
}
