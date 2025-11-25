package com.y11i.springcommddd.communities.application.port.out;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.List;

public interface LoadCommunityModeratorsPort {
    /**
     * 주어진 커뮤니티의 모더레이터 목록을 조회합니다.
     *
     * @param communityId 커뮤니티 식별자
     * @return 해당 커뮤니티에 부여된 모더레이터 엔트리 목록
     */
    List<CommunityModerator> loadByCommunityId(CommunityId communityId);

    // 2-7: 내가 모더레이터인 커뮤니티 목록
    List<CommunityModerator> loadByMemberId(MemberId memberId);
}
