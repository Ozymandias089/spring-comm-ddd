package com.y11i.springcommddd.communities.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidCommunityNameKey extends BaseException {
    public InvalidCommunityNameKey(String message) {
        super(ErrorCode.COMMUNITY_NAME_KEY_INVALID, message);
    }
}
