package com.y11i.springcommddd.posts.infrastructure;

import com.y11i.springcommddd.posts.application.port.out.SavePostPort;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostPersistenceAdapter implements SavePostPort {
    private final PostRepository postRepository;

    @Override
    @Transactional
    public Post save(Post post) {
        return postRepository.save(post);
    }
}
