package com.y11i.springcommddd.communities.application.port.out;

import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;

public interface SaveCommunityModeratorsPort {
    /**
     * 커뮤니티 모더레이터 엔티티를 저장합니다.
     *
     * @param moderator 저장할 모더레이터 애그리게잇
     * @return 저장된 모더레이터 애그리게잇 (ID, createdAt 등이 채워진 상태)
     */
    CommunityModerator save(CommunityModerator moderator);

    void delete(CommunityModerator moderator);
}
