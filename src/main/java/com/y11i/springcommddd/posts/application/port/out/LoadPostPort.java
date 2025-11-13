package com.y11i.springcommddd.posts.application.port.out;

import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;

import java.util.Optional;

public interface LoadPostPort {
    Optional<Post> loadById(PostId postId);
}
