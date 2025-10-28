package com.y11i.springcommddd.posts.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class ArchivedPostModificationNotAllowed extends BaseException {
    public ArchivedPostModificationNotAllowed(String detail) {
        super(ErrorCode.POST_ARCHIVED_MODIFICATION_FORBIDDEN, detail);
    }
}
