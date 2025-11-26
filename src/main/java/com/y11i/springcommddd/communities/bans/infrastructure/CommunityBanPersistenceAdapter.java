package com.y11i.springcommddd.communities.bans.infrastructure;

import com.y11i.springcommddd.communities.bans.application.port.out.LoadBanPort;
import com.y11i.springcommddd.communities.bans.application.port.out.SaveBanPort;
import com.y11i.springcommddd.communities.bans.domain.CommunityBan;
import com.y11i.springcommddd.communities.bans.domain.CommunityBanRepository;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityBanPersistenceAdapter implements SaveBanPort, LoadBanPort {
    private final CommunityBanRepository communityBanRepository;

    @Override
    public List<CommunityBan> findBansByCommunityId(CommunityId communityId) {
        return communityBanRepository.findByCommunityId(communityId);
    }

    @Override
    public Optional<CommunityBan> loadActiveBan(CommunityId communityId, MemberId memberId) {
        return communityBanRepository.findActiveBan(communityId, memberId);
    }

    @Override
    @Transactional
    public CommunityBan saveBan(CommunityBan ban) {
        return communityBanRepository.save(ban);
    }
}
