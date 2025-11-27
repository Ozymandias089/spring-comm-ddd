package com.y11i.springcommddd.comments.application.port.out;

import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;

import java.util.Optional;

public interface LoadPostForCommentPort {
    Optional<Post> loadById(PostId postId);
}
