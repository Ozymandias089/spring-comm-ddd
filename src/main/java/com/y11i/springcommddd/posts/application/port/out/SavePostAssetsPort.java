package com.y11i.springcommddd.posts.media.application.port.out;

import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.media.domain.PostAsset;

import java.util.List;

/**
 * 게시글 미디어 자산(PostAsset)을 영속화하기 위한 포트.
 */
public interface SavePostAssetsPort {
    /**
     * 단일 자산 저장.
     */
    PostAsset save(PostAsset asset);

    /**
     * 여러 자산을 한꺼번에 저장.
     */
    List<PostAsset> saveAll(List<PostAsset> assets);

    /**
     * 특정 게시글의 기존 자산을 모두 삭제.
     * (재업로드/교체 시 사용할 수 있음)
     */
    void deleteAllByPostId(PostId postId);
}
