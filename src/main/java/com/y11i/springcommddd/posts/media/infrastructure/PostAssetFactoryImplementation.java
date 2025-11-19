package com.y11i.springcommddd.posts.media.infrastructure;

import com.y11i.springcommddd.posts.application.port.out.PostAssetFactory;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.media.model.AssetMeta;
import com.y11i.springcommddd.posts.media.domain.MediaType;
import com.y11i.springcommddd.posts.media.domain.PostAsset;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostAssetFactoryImplementation implements PostAssetFactory {
    @Override
    public PostAsset fromMeta(PostId postId, AssetMeta meta) {
        String srcUrl = buildSrcUrlFromFileName(meta.fileName());

        if (meta.mediaType() == MediaType.IMAGE) {
            return PostAsset.image(
                    postId,
                    meta.fileSize(),
                    meta.fileName(),
                    meta.displayOrder(),
                    srcUrl,
                    meta.mimeType(),
                    null,
                    null,
                    null,
                    null
            );

        } else if (meta.mediaType() == MediaType.VIDEO) {
            return PostAsset.video(
                    postId,
                    meta.fileSize(),
                    meta.fileName(),
                    meta.displayOrder(),
                    srcUrl,
                    meta.mimeType(),
                    null,
                    null,
                    null,
                    null,
                    null
            );

        } else {
            throw new IllegalArgumentException("Unsupported mediaType: " + meta.mediaType());
        }
    }

    private String buildSrcUrlFromFileName(String fileName) {
        return fileName;
        // 나중에 CDN 쓰면 여기만 고치면 됨
        // return "https://cdn.springcomm.app/media/" + fileName;
    }
}
