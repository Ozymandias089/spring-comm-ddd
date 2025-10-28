package com.y11i.springcommddd.communities.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class CommunityStatusTransitionNotAllowed extends BaseException {
    public CommunityStatusTransitionNotAllowed(String message) {
        super(ErrorCode.COMMUNITY_STATUS_TRANSITION_FORBIDDEN, message);
    }
}
