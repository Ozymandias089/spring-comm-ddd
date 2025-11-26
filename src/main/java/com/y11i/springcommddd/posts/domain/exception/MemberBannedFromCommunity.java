package com.y11i.springcommddd.posts.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class MemberBannedFromCommunity extends BaseException {
    public MemberBannedFromCommunity(String message) {
        super(ErrorCode.MEMBER_BANNED_FROM_COMMUNITY, message);
    }
}
