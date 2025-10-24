package com.y11i.springcommddd.posts.media.domain;

import com.y11i.springcommddd.posts.domain.PostId;

import java.util.List;
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
}
