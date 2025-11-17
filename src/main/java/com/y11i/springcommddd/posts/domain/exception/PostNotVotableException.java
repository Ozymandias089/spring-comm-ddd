package com.y11i.springcommddd.posts.domain.exception;

import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.PostStatus;
import lombok.Getter;

public class PostNotVotableException extends RuntimeException {
    @Getter private final PostId postId;
    @Getter private final PostStatus status;

    public PostNotVotableException(PostId postId, PostStatus status) {
        super("Post " + postId.stringify() + " is not votable in status " + status);
        this.postId = postId;
        this.status = status;
    }
}
