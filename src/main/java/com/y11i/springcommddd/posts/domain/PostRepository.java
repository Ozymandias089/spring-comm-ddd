package com.y11i.springcommddd.posts.domain;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PostRepository {
    Post save(Post post);
    Optional<Post> findById(PostId id);
    List<Post> findAll();
    List<Post> findByAuthorId(MemberId authorId);
    Page<Post> findByAuthorId(MemberId authorId, Pageable pageable);
    void delete(Post post);
    List<Post> findByCommunityId(CommunityId communityId);
    Page<Post> findByCommunityId(CommunityId communityId, Pageable pageable);
}
