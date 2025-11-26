package com.y11i.springcommddd.communities.bans.application.port.out;

import com.y11i.springcommddd.communities.bans.domain.CommunityBan;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.List;
import java.util.Optional;

public interface LoadBanPort {
    List<CommunityBan> findBansByCommunityId(CommunityId communityId);
    Optional<CommunityBan> loadActiveBan(CommunityId communityId, MemberId memberId);
}
