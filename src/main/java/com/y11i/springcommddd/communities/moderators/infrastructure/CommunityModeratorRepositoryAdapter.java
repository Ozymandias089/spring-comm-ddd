package com.y11i.springcommddd.communities.moderators.infrastructure;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModeratorRepository;
import com.y11i.springcommddd.iam.domain.MemberId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class CommunityModeratorRepositoryAdapter implements CommunityModeratorRepository {
    private final JpaCommunityModeratorRepository jpaCommunityModeratorRepository;
    public CommunityModeratorRepositoryAdapter(JpaCommunityModeratorRepository jpaCommunityModeratorRepository) {
        this.jpaCommunityModeratorRepository = jpaCommunityModeratorRepository;
    }

    @Override @Transactional
    public CommunityModerator save(CommunityModerator mod) { return jpaCommunityModeratorRepository.save(mod); }

    @Override @Transactional
    public void delete(CommunityModerator mod) { jpaCommunityModeratorRepository.delete(mod); }

    @Override
    public boolean existsByCommunityIdAndMemberId(CommunityId communityId, MemberId memberId) {
        return jpaCommunityModeratorRepository.existsByCommunityIdAndMemberId(communityId, memberId);
    }

    @Override
    public List<CommunityModerator> findByCommunityId(CommunityId communityId) { return jpaCommunityModeratorRepository.findByCommunityId(communityId); }

    @Override
    public List<CommunityModerator> findByMemberId(MemberId memberId) { return jpaCommunityModeratorRepository.findByMemberId(memberId); }

    @Override
    public Optional<CommunityModerator> findOneByCommunityIdAndMemberId(CommunityId communityId, MemberId memberId) {
        return jpaCommunityModeratorRepository.findByCommunityIdAndMemberId(communityId, memberId);
    }
}
