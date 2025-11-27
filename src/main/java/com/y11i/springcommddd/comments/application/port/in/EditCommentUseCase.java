package com.y11i.springcommddd.comments.application.port.in;

import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.iam.domain.MemberId;

public interface EditCommentUseCase {
    void edit(EditCommentCommand cmd);

    record EditCommentCommand(
            CommentId commentId,
            MemberId actorId,
            String body
    ){}
}
