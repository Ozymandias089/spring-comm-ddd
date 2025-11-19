package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.media.model.AssetMeta;

import java.util.List;

/**
 * 게시글 작성 유스케이스.
 * <p>
 *     이 유스케이스는 새 게시글을 생성한다.
 * </p>
 */
public interface CreatePostDraftUseCase {
    PostId createTextDraft(CreateTextDraftCommand cmd);
    PostId createLinkDraft(CreateLinkDraftCommand cmd);
    PostId createMediaDraft(CreateMediaDraftCommand cmd);

    /**
     * 텍스트 게시글 생성 커맨드.
     */
    record CreateTextDraftCommand(
            CommunityId communityId,
            MemberId authorId,
            String title,
            String content
    ) {}

    /**
     * 링크 게시글 생성 커맨드.
     */
    record CreateLinkDraftCommand(
            CommunityId communityId,
            MemberId authorId,
            String title,
            String link
    ) {}

    /**
     * 미디어(이미지/영상) 게시글 생성 커맨드.
     * content는 캡션/본문으로 사용 가능.
     */
    record CreateMediaDraftCommand(
            CommunityId communityId,
            MemberId authorId,
            String title,
            String content,
            List<AssetMeta> assets
    ) {}

}
