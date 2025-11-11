package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.dto.PostDTO;

import java.util.UUID;

/**
 * 게시글 생성/수정/상태전이(게시/보관/복구) 유스케이스.
 */
public interface ManagePostUseCase {
    PostDTO create(CreateCommand cmd);
    PostDTO rename(ModifyTitleCommand cmd);
    PostDTO rewrite(ModifyContentCommand cmd);

    void publish(PublishCommand cmd);
    void archive(ArchiveCommand cmd);
    void restore(RestoreCommand cmd);

    // ---------- Commands ----------
    record CreateCommand(CommunityId communityId, MemberId authorId, String title, String content) {}
    record ModifyTitleCommand(UUID postId, MemberId actorId, String newTitle) {}
    record ModifyContentCommand(UUID postId, MemberId actorId, String newContent) {}
    record PublishCommand(UUID postId, MemberId actorId) {}
    record ArchiveCommand(UUID postId, MemberId actorId) {}
    record RestoreCommand(UUID postId, MemberId actorId) {}
}
