package com.y11i.springcommddd.comments.infrastructure;

import com.y11i.springcommddd.comments.domain.Comment;
import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.comments.domain.CommentRepository;
import com.y11i.springcommddd.posts.domain.PostId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class CommentRepositoryAdapter implements CommentRepository {

    private final JpaCommentRepository jpa;

    public CommentRepositoryAdapter(JpaCommentRepository jpa) {
        this.jpa = jpa;
    }

    @Override @Transactional
    public Comment save(Comment c) { return jpa.save(c); }

    @Override
    public Optional<Comment> findById(CommentId id) { return jpa.findById(id); }

    @Override
    public Page<Comment> findRootsByPostId(PostId postId, Pageable pageable) {
        return jpa.findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(postId, pageable);
    }

    @Override
    public List<Comment> findByParentId(CommentId parentId) {
        return jpa.findByParentIdOrderByCreatedAtAsc(parentId);
    }

    @Override
    public Page<Comment> findByPostId(PostId postId, Pageable pageable) {
        return jpa.findByPostId(postId, pageable);
    }
}
