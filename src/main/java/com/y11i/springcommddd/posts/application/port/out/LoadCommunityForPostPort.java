package com.y11i.springcommddd.posts.application.port.out;

import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityId;

import java.util.Optional;

/**
 * 게시글 작성 시 커뮤니티를 검증/조회하기 위한 포트.
 */
public interface LoadCommunityForPostPort {
    /**
     * 커뮤니티 식별자로 커뮤니티를 조회합니다.
     *
     * @param communityId 커뮤니티 ID
     * @return 존재하면 Community, 없으면 Optional.empty()
     */
    Optional<Community> loadById(CommunityId communityId);
}
