package com.y11i.springcommddd.posts.media.domain;

import com.y11i.springcommddd.posts.domain.PostId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * PostAsset 리포지토리 (도메인 계약).
 */
public interface PostAssetRepository {
    PostAsset save(PostAsset a);
    Optional<PostAsset> findById(PostAssetId id);

    /**
     * 게시글에 속한 자산을 표시 순서로 정렬하여 반환합니다.
     */
    List<PostAsset> findByPostIdOrderByDisplayOrder(PostId postId);

    /**
     * 목록 화면 등에서 첫 번째(대표) 자산만 필요할 때 사용합니다.
     */
    Optional<PostAsset> findFirstByPostId(PostId postId);

    void delete(PostAsset a);

    // === 보강 ===
    long countByPostId(PostId postId);
    Optional<Integer> findMaxDisplayOrder(PostId postId);
    boolean existsByPostIdAndDisplayOrder(PostId postId, int displayOrder);

    // 재정렬 (삽입/삭제 시 자리 밀고 당기기)
    int shiftRightFromOrder(PostId postId, int fromOrder);
    int shiftLeftAfterOrder(PostId postId, int removedOrder);

    // 상태/조회 편의
    List<PostAsset> findByPostIdAndStatus(PostId postId, ProcessingStatus status);
    Page<PostAsset> findPageByPostId(PostId postId, Pageable pageable);

    // 특정 variant 존재/조회
    boolean existsVariantByName(PostId postId, String variantName); // poster/hls 등
    List<PostAsset> findByPostIdAndVariantName(PostId postId, String variantName);

    // 일괄 순서 반영(드래그앤드롭)
    int bulkUpdateOrders(PostId postId, Map<PostAssetId, Integer> newOrders);

    // 정리
    int deleteAllByPostId(PostId postId);
}
