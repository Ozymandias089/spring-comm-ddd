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

    /**
     * 텍스트 게시글 수정.
     * - title / content 둘 중 하나 혹은 둘 다 수정 가능
     * - null인 필드는 수정하지 않음
     */
    PostDTO editTextPost(EditTextPostCommand cmd);

    /**
     * 링크 게시글 수정.
     * - title만 수정 가능
     * - 링크(URL)는 강한 불변성 정책으로 인해 수정 불가
     */
    PostDTO editLinkPost(EditLinkPostCommand cmd);

    /**
     * 미디어 게시글 수정.
     * - title 수정 가능
     * - content는 미디어 게시글의 캡션/본문으로 사용된다고 가정하고 수정 허용
     * - 미디어 파일 자체(src/variants)는 수정 불가
     */
    PostDTO editMediaPost(EditMediaPostCommand cmd);

    record PublishPostCommand(PostId postId, MemberId actorId) {}
    record ArchivePostCommand(PostId postId, MemberId actorId) {}
    record RestorePostCommand(PostId postId, MemberId actorId) {}

    // TEXT 전용: 제목/내용 둘 다 수정 가능
    record EditTextPostCommand(
            PostId postId,
            MemberId actorId,
            Title newTitle,      // nullable
            Content newContent   // nullable
    ) {}

    // LINK 전용: 제목만 수정 (링크 URL은 고정)
    record EditLinkPostCommand(
            PostId postId,
            MemberId actorId,
            Title newTitle       // nullable (null이면 수정 없음)
    ) {}

    // MEDIA 전용: 제목 + 캡션(Content) 수정
    record EditMediaPostCommand(
            PostId postId,
            MemberId actorId,
            Title newTitle,      // nullable
            Content newContent   // nullable (캡션/본문)
    ) {}

}
