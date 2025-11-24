package com.y11i.springcommddd.communities.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;

public interface ActivateCommunityUseCase {
    CommunityId activateCommunity(ActivateCommunityCommand cmd);

    record ActivateCommunityCommand(MemberId actorId, CommunityId communityId){}
}
