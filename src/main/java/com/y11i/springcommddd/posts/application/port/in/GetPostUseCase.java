package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.dto.PostDTO;

import java.util.Optional;
import java.util.UUID;

/**
 * 단건 조회 유스케이스.
 */
public interface GetPostUseCase {
    Optional<PostDTO> getById(UUID postId, Optional<MemberId> viewer);
}
