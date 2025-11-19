package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
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

    PostId createAndPublishText(CreateAndPublishTextCommand cmd);

    PostId createAndPublishLink(CreateAndPublishLinkCommand cmd);

    PostId createAndPublishMedia(CreateAndPublishMediaCommand cmd);

    // ----------------------------------------------------------------------
    // Commands
    // ----------------------------------------------------------------------

    /**
     * 텍스트 게시글을 바로 게시하기 위한 커맨드.
     */
    record CreateAndPublishTextCommand(
            CommunityId communityId,
            MemberId authorId,
            String title,
            String content
    ) {}

    /**
     * 링크 게시글을 바로 게시하기 위한 커맨드.
     */
    record CreateAndPublishLinkCommand(
            CommunityId communityId,
            MemberId authorId,
            String title,
            String link
    ) {}

    /**
     * 미디어(이미지/영상) 게시글을 바로 게시하기 위한 커맨드.
     *
     * <p>
     * assets는 기존 {@link AssetMeta} 와 동일한 메타 정보를 사용한다.
     * 컨트롤러에서는 PostAssetUploadDTO → AssetMeta 로 변환하여 넘겨주면 된다.
     * </p>
     */
    record CreateAndPublishMediaCommand(
            CommunityId communityId,
            MemberId authorId,
            String title,
            String content,
            List<AssetMeta> assets
    ) {}
}