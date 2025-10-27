package com.y11i.springcommddd.communities.moderators.infrastructure;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModeratorId;
import com.y11i.springcommddd.iam.domain.MemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaCommunityModeratorRepository extends JpaRepository<CommunityModerator, CommunityModeratorId> {
    boolean existsByCommunityIdAndMemberId(CommunityId communityId, MemberId memberId);
    List<CommunityModerator> findByCommunityId(CommunityId communityId);
    List<CommunityModerator> findByMemberId(MemberId memberId);
    Optional<CommunityModerator> findByCommunityIdAndMemberId(CommunityId communityId, MemberId memberId);
}
