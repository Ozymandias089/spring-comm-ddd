package com.y11i.springcommddd.posts.infrastructure;

import com.y11i.springcommddd.posts.application.port.out.LoadPostPort;
import com.y11i.springcommddd.posts.application.port.out.SavePostPort;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostPersistenceAdapter implements SavePostPort, LoadPostPort {
    private final PostRepository postRepository;

    @Override
    @Transactional
    public Post save(Post post) {
        return postRepository.save(post);
    }

    @Override
    public Optional<Post> loadById(PostId postId) {
        return postRepository.findById(postId);
    }
}
