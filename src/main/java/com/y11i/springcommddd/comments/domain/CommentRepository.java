package com.y11i.springcommddd.comments.domain;

import com.y11i.springcommddd.posts.domain.PostId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {
    Comment save(Comment c);
    Optional<Comment> findById(CommentId id);

    // 루트 댓글(부모 없는) 목록
    Page<Comment> findRootsByPostId(PostId postId, Pageable pageable);

    // 특정 부모에 대한 자식 목록
    List<Comment> findByParentId(CommentId parentId);

    // 포스트 전체 댓글 조회(필요 시)
    Page<Comment> findByPostId(PostId postId, Pageable pageable);
}
