package com.y11i.springcommddd.posts.application.port.out;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;

public interface CheckCommunityBanPort {
    /**
     * 주어진 커뮤니티에서 멤버가 밴 상태가 아닌지 검증한다.
     * 밴 상태라면 예외를 던진다.
     */
    void ensureNotBanned(CommunityId communityId, MemberId memberId);
}
