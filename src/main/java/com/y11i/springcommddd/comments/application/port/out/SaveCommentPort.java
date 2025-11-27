package com.y11i.springcommddd.comments.application.port.out;

import com.y11i.springcommddd.comments.domain.Comment;

public interface SaveCommentPort {
    Comment save(Comment comment);
}
