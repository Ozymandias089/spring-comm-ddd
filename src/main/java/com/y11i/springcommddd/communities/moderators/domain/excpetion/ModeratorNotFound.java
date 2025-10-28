package com.y11i.springcommddd.communities.moderators.domain.excpetion;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class ModeratorNotFound extends BaseException {
    public ModeratorNotFound(String message) {
        super(ErrorCode.MODERATOR_NOT_FOUND, message);
    }
}
