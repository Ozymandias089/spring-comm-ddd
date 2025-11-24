package com.y11i.springcommddd.communities.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class DuplicateCommunityNameKey extends BaseException {
    public DuplicateCommunityNameKey(String message) {
        super(ErrorCode.DUPLICATE_COMMUNITY_NAME_KEY, message);
    }
}
