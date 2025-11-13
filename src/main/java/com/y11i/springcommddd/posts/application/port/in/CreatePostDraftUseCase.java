package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.Content;
import com.y11i.springcommddd.posts.domain.LinkUrl;
import com.y11i.springcommddd.posts.domain.Title;
import com.y11i.springcommddd.posts.dto.internal.PostDTO;
import com.y11i.springcommddd.posts.media.domain.MediaType;

import java.util.List;

/**
 * 게시글 작성 유스케이스.
 * <p>
 *     이 유스케이스는 새 게시글을 생성한다.
 * </p>
 */
public interface CreatePostUseCase {
    PostDTO createTextPost(CreateTextPostCommand cmd);
    PostDTO createLinkPost(CreateLinkPostCommand cmd);
    PostDTO createMediaPost(CreateMediaPostCommand cmd);

    /**
     * 텍스트 게시글 생성 커맨드.
     */
    record CreateTextPostCommand(
            CommunityId communityId,
            MemberId authorId,
            Title title,
            Content content
    ) {}

    /**
     * 링크 게시글 생성 커맨드.
     */
    record CreateLinkPostCommand(
            CommunityId communityId,
            MemberId authorId,
            Title title,
            LinkUrl link
    ) {}

    /**
     * 미디어(이미지/영상) 게시글 생성 커맨드.
     * content는 캡션/본문으로 사용 가능.
     */
    record CreateMediaPostCommand(
            CommunityId communityId,
            MemberId authorId,
            Title title,
            Content content,
            List<AssetMeta> assets
    ) {}

    /**
     * 업로드된 미디어 자산 메타정보.
     * (컨트롤러에서 PostAssetUploadDTO -> AssetMeta로 변환해서 넘겨주면 됨)
     */
    record AssetMeta(
            MediaType mediaType,  // IMAGE | VIDEO
            int displayOrder,
            long fileSize,
            String fileName,
            String mimeType
    ) {}
}
