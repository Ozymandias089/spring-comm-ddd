package com.y11i.springcommddd.communities.domain.exception;


import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class CommunityNotFound extends BaseException {
    public CommunityNotFound(String message) {
        super(ErrorCode.COMMUNITY_NOT_FOUND, message);
    }
}
