package com.y11i.springcommddd.posts.media.infrastructure;

import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.media.domain.PostAsset;
import com.y11i.springcommddd.posts.media.domain.PostAssetId;
import com.y11i.springcommddd.posts.media.domain.PostAssetRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * PostAsset 도메인 리포지토리 어댑터.
 * <p>트랜잭션 읽기 전용 기본, 쓰기 메서드에만 @Transactional 부여.</p>
 */
@Transactional(readOnly = true)
public class PostAssetRepositoryAdapter implements PostAssetRepository {
    private final JpaPostAssetRepository jpaPostAssetRepository;
    public PostAssetRepositoryAdapter(JpaPostAssetRepository jpaPostAssetRepository) {
        this.jpaPostAssetRepository = jpaPostAssetRepository;
    }

    @Override @Transactional
    public PostAsset save(PostAsset a) {
        return jpaPostAssetRepository.save(a);
    }

    @Override
    public Optional<PostAsset> findById(PostAssetId id) {
        return jpaPostAssetRepository.findById(id);
    }

    @Override
    public List<PostAsset> findByPostIdOrderByDisplayOrder(PostId postId) {
        return jpaPostAssetRepository.findByPostIdOrderByDisplayOrder(postId);
    }

    @Override
    public Optional<PostAsset> findFirstByPostId(PostId postId) {
        return jpaPostAssetRepository.findFirstByPostIdOrderByDisplayOrderAsc(postId);
    }

    @Override @Transactional
    public void delete(PostAsset a) {
        jpaPostAssetRepository.delete(a);
    }
}
