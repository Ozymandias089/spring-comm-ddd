package com.y11i.springcommddd.communities.bans.domain;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.List;
import java.util.Optional;

public interface CommunityBanRepository {

    CommunityBan save(CommunityBan ban);

    Optional<CommunityBan> loadById(CommunityBanId id);

    List<CommunityBan> findByCommunityId(CommunityId communityId);

    Optional<CommunityBan> findActiveBan(CommunityId communityId, MemberId memberId);

    void delete(CommunityBan ban);

    boolean existsActiveBan(CommunityId communityId, MemberId memberId);
}