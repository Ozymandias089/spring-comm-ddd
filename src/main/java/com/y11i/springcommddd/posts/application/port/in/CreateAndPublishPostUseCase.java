package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.domain.PostType;
import com.y11i.springcommddd.posts.media.model.AssetMeta;

import java.util.List;

/**
 * 게시글을 "바로 게재" 상태로 생성하는 유스케이스.
 *
 * <p>
 *  - 기존의 {@link CreatePostDraftUseCase} 를 통해 초안(DRAFT)을 생성하고,
 *  - {@link ManagePostUseCase} 를 통해 즉시 게시(PUBLISHED) 상태로 전환한다.
 * </p>
 *
 * <p>
 * 프런트엔드 입장에서는 이 유스케이스를 사용하여
 * 한 번의 요청으로 "바로 게시" 버튼 동작을 구현할 수 있다.
 * </p>
 */
public interface CreateAndPublishPostUseCase {

    PostId createAndPublish(CreateAndPublishCommand cmd);

    record CreateAndPublishCommand(
            CommunityId communityId,
            MemberId authorId,
            PostType type,
            String title,
            String content,          // TEXT, MEDIA에서 사용
            String link,             // LINK에서 사용
            List<AssetMeta> assets   // MEDIA에서 사용
    ) {}
}