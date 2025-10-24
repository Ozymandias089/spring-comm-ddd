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

/**
 * {@link PostRepository}의 인프라스트럭처 계층 구현체.
 * <p>
 * 도메인 계층의 {@link PostRepository}를
 * JPA 기반 리포지토리 {@link JpaPostRepository}로 어댑팅하여 실제 데이터 접근을 수행합니다.
 * </p>
 *
 * <p><b>특징:</b></p>
 * <ul>
 *     <li>도메인 계층이 JPA 구현 세부사항에 직접 의존하지 않음</li>
 *     <li>읽기 작업에는 {@code readOnly = true} 트랜잭션 적용</li>
 *     <li>쓰기 작업(저장, 삭제)은 별도의 트랜잭션으로 처리</li>
 * </ul>
 *
 * @see JpaPostRepository
 * @see Post
 * @see MemberId
 * @see CommunityId
 * @author y11
 */
@Repository
@Transactional(readOnly = true)
public class PostRepositoryAdapter implements PostRepository {
    private final JpaPostRepository jpaPostRepository;

    /**
     * JPA 리포지토리를 주입받습니다.
     *
     * @param jpaPostRepository JPA 기반 게시글 리포지토리
     */
    public PostRepositoryAdapter(JpaPostRepository jpaPostRepository) {
        this.jpaPostRepository = jpaPostRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional // write
    public Post save(Post post) {
        return jpaPostRepository.save(post);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Post> findById(PostId id) {
        return jpaPostRepository.findById(id);
    }

    /** {@inheritDoc} */
    @Override
    public List<Post> findAll() {
        return jpaPostRepository.findAll();
    }

    /** {@inheritDoc} */
    @Override
    public List<Post> findByAuthorId(MemberId authorId) {
        return jpaPostRepository.findByAuthorId(authorId);
    }

    /** {@inheritDoc} */
    @Override
    public Page<Post> findByAuthorId(MemberId authorId, Pageable pageable) {
        return jpaPostRepository.findByAuthorId(authorId, pageable);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void delete(Post post) {
        jpaPostRepository.delete(post);
    }

    /** {@inheritDoc} */
    @Override
    public List<Post> findByCommunityId(CommunityId communityId) {
        return jpaPostRepository.findByCommunityId(communityId);
    }

    /** {@inheritDoc} */
    @Override
    public Page<Post> findByCommunityId(CommunityId communityId, Pageable pageable) {
        return jpaPostRepository.findByCommunityId(communityId, pageable);
    }
}
