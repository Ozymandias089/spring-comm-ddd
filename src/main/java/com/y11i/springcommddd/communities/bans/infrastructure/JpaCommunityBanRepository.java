package com.y11i.springcommddd.communities.bans.infrastructure;

import com.y11i.springcommddd.communities.bans.domain.CommunityBan;
import com.y11i.springcommddd.communities.bans.domain.CommunityBanId;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaCommunityBanRepository extends JpaRepository<CommunityBan, CommunityBanId> {

    List<CommunityBan> findByCommunityId(CommunityId communityId);

    Optional<CommunityBan> findByCommunityIdAndBannedMemberIdAndLiftedAtIsNull(CommunityId communityId, MemberId memberId);

    boolean existsByCommunityIdAndBannedMemberIdAndLiftedAtIsNull(CommunityId communityId, MemberId memberId);
}