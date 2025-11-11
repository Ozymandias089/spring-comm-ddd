package com.y11i.springcommddd.posts.application.port.out;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;

public interface CommunityAccessPolicyPort {
    boolean canPost(MemberId actor, CommunityId communityId);
    boolean canRead(MemberId actorOrNull, CommunityId communityId);
    boolean canModerate(MemberId actor, CommunityId communityId); // 선택
}
