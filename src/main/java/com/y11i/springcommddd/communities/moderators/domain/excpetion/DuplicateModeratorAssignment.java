package com.y11i.springcommddd.communities.moderators.domain.excpetion;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class DuplicateModeratorAssignment extends BaseException {
    public DuplicateModeratorAssignment(String message) {
        super(ErrorCode.MODERATOR_DUPLICATE, message);
    }
}
