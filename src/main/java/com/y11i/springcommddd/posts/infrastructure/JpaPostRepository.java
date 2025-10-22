package com.y11i.springcommddd.posts.infrastructure;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaPostRepository extends JpaRepository<Post, PostId>, PostRepository {
    List<Post> findByAuthorId(MemberId authorId);
    Page<Post> findByAuthorId(MemberId authorId, Pageable pageable);
}
