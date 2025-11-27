package com.y11i.springcommddd.comments.infrastructure;

import com.y11i.springcommddd.comments.application.port.out.LoadCommentPort;
import com.y11i.springcommddd.comments.application.port.out.LoadPostForCommentPort;
import com.y11i.springcommddd.comments.application.port.out.SaveCommentPort;
import com.y11i.springcommddd.comments.domain.Comment;
import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.comments.domain.CommentRepository;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentPersistenceAdapter implements LoadCommentPort, SaveCommentPort, LoadPostForCommentPort {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Override
    public Optional<Comment> loadById(CommentId id) {
        return commentRepository.findById(id);
    }

    @Override
    public Optional<Post> loadById(PostId postId) {
        return postRepository.findById(postId);
    }

    @Override
    @Transactional
    public Comment save(Comment comment) {
        return commentRepository.save(comment);
    }
}
