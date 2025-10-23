package com.y11i.springcommddd.posts.infrastructure;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class PostRepositoryAdapter implements PostRepository {
    private final JpaPostRepository jpaPostRepository;

    public PostRepositoryAdapter(JpaPostRepository jpaPostRepository) {
        this.jpaPostRepository = jpaPostRepository;
    }

    @Override
    @Transactional // write
    public Post save(Post post) {
        return jpaPostRepository.save(post);
    }

    @Override
    public Optional<Post> findById(PostId id) {
        return jpaPostRepository.findById(id);
    }

    @Override
    public List<Post> findAll() {
        return jpaPostRepository.findAll();
    }

    /**
     * Search for all the posts by the same author as list
     * @param authorId ID of a member to search post with
     * @return Lists of all the post made by MemberId provided with the parameter
     */
    @Override
    public List<Post> findByAuthorId(MemberId authorId) {
        return jpaPostRepository.findByAuthorId(authorId);
    }

    /**
     * @param authorId Author ID to search pages
     * @param pageable pageable
     * @return posts with the provided authorId as pages
     */
    @Override
    public Page<Post> findByAuthorId(MemberId authorId, Pageable pageable) {
        return jpaPostRepository.findByAuthorId(authorId, pageable);
    }

    /**
     * @param post Post to Hard delete(do not use it for normal delete post Use case)
     */
    @Override
    public void delete(Post post) {

    }

    /**
     * @param communityId Id of a Community To find posts by
     * @return List of all the posts with the id matching parameter
     */
    @Override
    public List<Post> findByCommunityId(CommunityId communityId) {
        return jpaPostRepository.findByCommunityId(communityId);
    }

    /**
     * @param communityId Id of a Community as Object
     * @param pageable Page object
     * @return All the posts matching parameter as Page objects
     */
    @Override
    public Page<Post> findByCommunityId(CommunityId communityId, Pageable pageable) {
        return jpaPostRepository.findByCommunityId(communityId, pageable);
    }
}
