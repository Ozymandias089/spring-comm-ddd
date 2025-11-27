package com.y11i.springcommddd.comments.application.port.in;

import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.iam.domain.MemberId;

public interface DeleteCommentUseCase {
    void delete(DeleteCommentCommand cmd);

    record DeleteCommentCommand(
            CommentId commentId,
            MemberId actorId
    ){}
}
