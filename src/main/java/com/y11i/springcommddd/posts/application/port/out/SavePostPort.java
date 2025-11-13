package com.y11i.springcommddd.posts.application.port.out;

import com.y11i.springcommddd.posts.domain.Post;

/**
 * 게시글(Post) 애그리게잇을 영속화하기 위한 포트.
 */
public interface SavePostPort {
    Post save(Post post);
}
