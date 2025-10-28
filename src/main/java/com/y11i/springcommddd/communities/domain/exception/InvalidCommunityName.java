package com.y11i.springcommddd.communities.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class InvalidCommunityName extends BaseException {
    public InvalidCommunityName(String detail) {
        super(ErrorCode.COMMUNITY_NAME_INVALID, detail);
    }
}
