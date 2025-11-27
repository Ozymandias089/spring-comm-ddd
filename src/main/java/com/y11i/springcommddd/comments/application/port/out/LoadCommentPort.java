package com.y11i.springcommddd.comments.application.port.out;

import com.y11i.springcommddd.comments.domain.Comment;
import com.y11i.springcommddd.comments.domain.CommentId;

import java.util.Optional;

public interface LoadCommentPort {
    Optional<Comment> loadById(CommentId id);
}
