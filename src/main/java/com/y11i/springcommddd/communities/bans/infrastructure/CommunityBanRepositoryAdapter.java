package com.y11i.springcommddd.communities.bans.infrastructure;

import com.y11i.springcommddd.communities.bans.domain.CommunityBan;
import com.y11i.springcommddd.communities.bans.domain.CommunityBanId;
import com.y11i.springcommddd.communities.bans.domain.CommunityBanRepository;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityBanRepositoryAdapter implements CommunityBanRepository {

    private final JpaCommunityBanRepository jpaRepo;

    @Override
    @Transactional
    public CommunityBan save(CommunityBan ban) {
        return jpaRepo.save(ban);
    }

    @Override
    public Optional<CommunityBan> loadById(CommunityBanId id) {
        return jpaRepo.findById(id);
    }

    @Override
    public List<CommunityBan> findByCommunityId(CommunityId communityId) {
        return jpaRepo.findByCommunityId(communityId);
    }

    @Override
    public Optional<CommunityBan> findActiveBan(CommunityId communityId, MemberId memberId) {
        return jpaRepo.findByCommunityIdAndBannedMemberIdAndLiftedAtIsNull(communityId, memberId);
    }

    @Override
    public boolean existsActiveBan(CommunityId communityId, MemberId memberId) {
        return jpaRepo.existsByCommunityIdAndBannedMemberIdAndLiftedAtIsNull(communityId, memberId);
    }

    @Override
    @Transactional
    public void delete(CommunityBan ban) {
        jpaRepo.delete(ban);
    }
}