package com.y11i.springcommddd.posts.media.infrastructure;

import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.media.domain.PostAsset;
import com.y11i.springcommddd.posts.media.domain.PostAssetId;
import com.y11i.springcommddd.posts.media.domain.PostAssetRepository;
import com.y11i.springcommddd.posts.media.domain.ProcessingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * PostAsset 도메인 리포지토리 어댑터.
 * <p>트랜잭션 읽기 전용 기본, 쓰기 메서드에만 @Transactional 부여.</p>
 */
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostAssetRepositoryAdapter implements PostAssetRepository {
    private final JpaPostAssetRepository jpaPostAssetRepository;

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

    @Override public long countByPostId(PostId postId){ return jpaPostAssetRepository.countByPostId(postId); }
    @Override public Optional<Integer> findMaxDisplayOrder(PostId postId){ return jpaPostAssetRepository.findMaxDisplayOrder(postId); }
    @Override public boolean existsByPostIdAndDisplayOrder(PostId postId, int order){ return jpaPostAssetRepository.existsByPostIdAndDisplayOrder(postId, order); }
    @Override @Transactional public int shiftRightFromOrder(PostId postId, int fromOrder){ return jpaPostAssetRepository.shiftRightFromOrder(postId, fromOrder); }
    @Override @Transactional public int shiftLeftAfterOrder(PostId postId, int removedOrder){ return jpaPostAssetRepository.shiftLeftAfterOrder(postId, removedOrder); }
    @Override public List<PostAsset> findByPostIdAndStatus(PostId postId, ProcessingStatus status){ return jpaPostAssetRepository.findByPostIdAndStatus(postId, status); }
    @Override public Page<PostAsset> findPageByPostId(PostId postId, Pageable pageable){ return jpaPostAssetRepository.findPageByPostId(postId, pageable); }
    @Override public boolean existsVariantByName(PostId postId, String name){ return jpaPostAssetRepository.existsVariantByName(postId, name); }
    @Override public List<PostAsset> findByPostIdAndVariantName(PostId postId, String name){ return jpaPostAssetRepository.findByPostIdAndVariantName(postId, name); }
    @Override @Transactional public int deleteAllByPostId(PostId postId){ return jpaPostAssetRepository.deleteByPostId(postId); }

    @Override @Transactional
    public int bulkUpdateOrders(PostId postId, Map<PostAssetId, Integer> newOrders){
        AtomicInteger updated = new AtomicInteger();
        for (var e : newOrders.entrySet()) {
            jpaPostAssetRepository.findById(e.getKey()).ifPresent(a -> { a.changeDisplayOrder(e.getValue()); updated.getAndIncrement(); });
        }
        return updated.get();
    }
}
