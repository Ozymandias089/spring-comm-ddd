package com.y11i.springcommddd.communities.moderators.domain.excpetion;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidModeratorAssignment extends BaseException {
    public InvalidModeratorAssignment(String message) {
        super(ErrorCode.MODERATOR_INVALID, message);
    }
}
