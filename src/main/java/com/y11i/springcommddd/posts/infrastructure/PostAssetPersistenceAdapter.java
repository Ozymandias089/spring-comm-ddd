package com.y11i.springcommddd.posts.media.infrastructure;

import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.media.application.port.out.SavePostAssetsPort;
import com.y11i.springcommddd.posts.media.domain.PostAsset;
import com.y11i.springcommddd.posts.media.domain.PostAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostAssetPersistenceAdapter implements SavePostAssetsPort {
    private final PostAssetRepository postAssetRepository;

    /**
     * 단일 자산 저장.
     *
     * @param asset Asset
     */
    @Override
    @Transactional
    public PostAsset save(PostAsset asset) {
        return postAssetRepository.save(asset);
    }

    /**
     * 여러 자산을 한꺼번에 저장.
     *
     * @param assets List of assets
     */
    @Override
    @Transactional
    public List<PostAsset> saveAll(List<PostAsset> assets) {
        return assets.stream()
                .map(postAssetRepository::save)
                .toList();
    }

    /**
     * 특정 게시글의 기존 자산을 모두 삭제.
     * (재업로드/교체 시 사용할 수 있음)
     *
     * @param postId PostIds
     */
    @Override
    @Transactional
    public void deleteAllByPostId(PostId postId) {
        postAssetRepository.deleteAllByPostId(postId);
    }
}
