package com.y11i.springcommddd.communities.moderators.domain;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.List;
import java.util.Optional;

public interface CommunityModeratorRepository {
    CommunityModerator save(CommunityModerator mod);
    void delete(CommunityModerator mod);

    boolean existsByCommunityIdAndMemberId(CommunityId communityId, MemberId memberId);
    List<CommunityModerator> findByCommunityId(CommunityId communityId);
    List<CommunityModerator> findByMemberId(MemberId memberId);
    Optional<CommunityModerator> findOneByCommunityIdAndMemberId(CommunityId communityId, MemberId memberId);
}
