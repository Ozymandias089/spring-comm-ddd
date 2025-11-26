package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.PostType;
import com.y11i.springcommddd.posts.media.model.AssetMeta;

import java.util.List;

/**
 * 게시글 작성 유스케이스.
 * <p>
 *     이 유스케이스는 새 게시글을 생성한다.
 * </p>
 */
public interface CreatePostDraftUseCase {
    PostId createDraft(CreateDraftCommand cmd);

    /**
     * 통합 초안 생성 커맨드.
     * <p>
     * type 에 따라 content / link / assets 의 사용 여부가 달라진다.
     */
    record CreateDraftCommand(
            CommunityId communityId,
            MemberId authorId,
            PostType type,
            String title,
            String content,          // TEXT, MEDIA에서 사용
            String link,             // LINK에서 사용
            List<AssetMeta> assets   // MEDIA에서 사용
    ) {}
}
