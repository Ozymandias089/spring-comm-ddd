package com.y11i.springcommddd.posts.media.application.port.out;

import com.y11i.springcommddd.posts.domain.PostId;

import java.util.Optional;

public interface MediaObjectStore {
    /**
     * 원본/파생본을 저장하고 식별자(키)를 반환.
     * url은 어댑터 레벨에서 키→서명/퍼블릭 URL로 변환 가능.
     */
    String putObject(String keyHint, byte[] bytes, String mimeType);

    /** 존재하면 삭제 */
    void deleteObject(String key);

    /** 퍼블릭 혹은 서명 URL 생성(정책에 따라) */
    String toPublicUrl(String key);

    long countByPostId(PostId postId);
    Optional<Integer> findMaxDisplayOrder(PostId postId);

}
