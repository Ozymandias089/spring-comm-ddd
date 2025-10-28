package com.y11i.springcommddd.comments.domain.exception;

import com.y11i.springcommddd.common.api.ErrorCode;
import com.y11i.springcommddd.common.exception.BaseException;

public class CommentDeletedModificationNotAllowed extends BaseException {
    public CommentDeletedModificationNotAllowed(String detail) {
        super(ErrorCode.COMMENT_DELETED_MODIFICATION_FORBIDDEN, detail);
    }
}
