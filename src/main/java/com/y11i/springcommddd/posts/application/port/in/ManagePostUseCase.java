package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.Content;
import com.y11i.springcommddd.posts.domain.LinkUrl;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.Title;
import com.y11i.springcommddd.posts.dto.internal.PostDTO;
import com.y11i.springcommddd.posts.media.domain.PostAssetId;

public interface ManagePostUseCase {

    // --- 상태 전이 ---

    /** DRAFT → PUBLISHED */
    PostDTO publish(PublishPostCommand cmd);

    /** PUBLISHED → ARCHIVED (보관) */
    PostDTO archive(ArchivePostCommand cmd);

    /** ARCHIVED → PUBLISHED (복구) */
    PostDTO restore(RestorePostCommand cmd);

    // --- 내용 수정 ---

    /** 제목 변경 */
    PostDTO rename(RenameTitleCommand cmd);

    /** 텍스트 본문 변경 (TEXT/MEDIA용) */
    PostDTO rewrite(RewriteContentCommand cmd);

    /** 링크 게시글의 링크 교체 (LINK용) */
    PostDTO replaceLink(ReplaceLinkCommand cmd);

    // --- 미디어 variant 조작 ---

    /**
     * 특정 PostAsset에 대한 variant upsert (없으면 생성, 있으면 갱신).
     * 예: poster, hls, mp4_720 등.
     */
    PostDTO upsertVariant(UpsertVariantCommand cmd);

    /**
     * 특정 PostAsset의 variant 제거.
     */
    PostDTO removeVariant(RemoveVariantCommand cmd);

    record PublishPostCommand(PostId postId, MemberId actorId) {}
    record ArchivePostCommand(PostId postId, MemberId actorId) {}
    record RestorePostCommand(PostId postId, MemberId actorId) {}

    record RenameTitleCommand(PostId postId, MemberId actorId, Title newTitle) {}

    record RewriteContentCommand(PostId postId, MemberId actorId, Content newContent) {}

    record ReplaceLinkCommand(PostId postId, MemberId actorId, LinkUrl newLink) {}

    record UpsertVariantCommand(
            PostId postId,
            MemberId actorId,
            PostAssetId assetId,
            String variantName,   // "poster", "hls", "mp4_720" 등
            String url,
            String mimeType,
            Integer width,
            Integer height
    ) {}

    record RemoveVariantCommand(
            PostId postId,
            MemberId actorId,
            PostAssetId assetId,
            String variantName
    ) {}
}
