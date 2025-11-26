package com.y11i.springcommddd.posts.infrastructure;

import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.domain.CommunityRepository;
import com.y11i.springcommddd.posts.application.port.out.LoadCommunityForPostPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommunityForPostAdapter implements LoadCommunityForPostPort {
    private final CommunityRepository communityRepository;

    /**
     * 커뮤니티 식별자로 커뮤니티를 조회합니다.
     *
     * @param communityNameKey 커뮤니티 ID
     * @return 존재하면 Community, 없으면 Optional.empty()
     */
    @Override
    public Optional<Community> loadByNameKey(CommunityNameKey communityNameKey) {
        return communityRepository.findByCommunityNameKey(communityNameKey);
    }

    @Override
    public Optional<Community> loadById(CommunityId communityId) {
        return communityRepository.findById(communityId);
    }
}
