package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.Content;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.Title;

public interface ManagePostUseCase {

    // --- 상태 전이 ---

    /** DRAFT → PUBLISHED */
    PostId publish(PublishPostCommand cmd);

    /** PUBLISHED → ARCHIVED (보관) */
    PostId archive(ArchivePostCommand cmd);

    /** ARCHIVED → PUBLISHED (복구) */
    PostId restore(RestorePostCommand cmd);

    void scrapDraft(ScrapDraftCommand cmd);

    /**
     * 게시글 수정 (TEXT / LINK / MEDIA 공용).
     * <p>
     * 규칙:
     *  - LINK  가 아닌 경우(TEXT / MEDIA): content 수정 허용
     *  - 모든 타입: title 수정 허용
     * <p>
     * newTitle / newContent는 null 허용이며,
     * null인 필드는 수정하지 않습니다.
     */
    PostId editPost(EditPostCommand cmd);


    record PublishPostCommand(PostId postId, MemberId actorId) {}
    record ArchivePostCommand(PostId postId, MemberId actorId) {}
    record RestorePostCommand(PostId postId, MemberId actorId) {}
    record EditPostCommand(
            PostId postId,
            MemberId actorId,
            Title newTitle,      // nullable
            Content newContent   // nullable
    ) {}
    record ScrapDraftCommand(PostId postId, MemberId actorId) {}
}
