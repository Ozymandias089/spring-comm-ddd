package com.y11i.springcommddd.comments.infrastructure;

import com.y11i.springcommddd.comments.domain.Comment;
import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.posts.domain.PostId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaCommentRepository extends JpaRepository<Comment, CommentId> {
    Page<Comment> findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(PostId postId, Pageable pageable);
    List<Comment> findByParentIdOrderByCreatedAtAsc(CommentId parentId);

    Page<Comment> findByPostId(PostId postId, Pageable pageable); // 선택
}
