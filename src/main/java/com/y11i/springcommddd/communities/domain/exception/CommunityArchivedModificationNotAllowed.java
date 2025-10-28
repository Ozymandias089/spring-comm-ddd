package com.y11i.springcommddd.communities.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class CommunityArchivedModificationNotAllowed extends BaseException {
    public CommunityArchivedModificationNotAllowed(String message) {
        super(ErrorCode.COMMUNITY_ARCHIVED_MODIFICATION_FORBIDDEN, message);
    }
}
