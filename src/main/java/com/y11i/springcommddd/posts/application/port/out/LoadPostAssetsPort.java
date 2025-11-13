package com.y11i.springcommddd.posts.application.port.out;

import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.media.domain.PostAsset;
import com.y11i.springcommddd.posts.media.domain.PostAssetId;

import java.util.List;
import java.util.Optional;

public interface LoadPostAssetsPort {
    Optional<PostAsset> loadById(PostAssetId assetId);

    List<PostAsset> loadByPostId(PostId postId);
}
