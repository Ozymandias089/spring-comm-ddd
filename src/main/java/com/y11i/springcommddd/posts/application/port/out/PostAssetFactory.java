package com.y11i.springcommddd.posts.application.port.out;

import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.media.model.AssetMeta;
import com.y11i.springcommddd.posts.media.domain.PostAsset;

public interface PostAssetFactory {
    PostAsset fromMeta(PostId postId, AssetMeta meta);
}
