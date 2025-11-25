package com.y11i.springcommddd.communities.application.internal;

import com.y11i.springcommddd.communities.application.port.internal.CommunityLookup;
import com.y11i.springcommddd.communities.application.port.out.LoadCommunityModeratorsPort;
import com.y11i.springcommddd.communities.application.port.out.LoadCommunityPort;
import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.domain.exception.CommunityNotFound;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CommunityLookupImpl implements CommunityLookup { // 구현체는 package-private도 괜찮음

    private final LoadCommunityPort loadCommunityPort;
    private final LoadCommunityModeratorsPort loadCommunityModeratorsPort;

    @Override
    public Community getByIdOrThrow(CommunityId id) {
        return loadCommunityPort.loadById(id)
                .orElseThrow(() -> new CommunityNotFound("Community not found: " + id.stringify()));
    }

    @Override
    public Community getByNameKeyOrThrow(CommunityNameKey nameKey) {
        return loadCommunityPort.loadByNameKey(nameKey)
                .orElseThrow(() -> new CommunityNotFound("Community not found: c/" + nameKey.value()));
    }

    @Override
    public List<CommunityModerator> getModerators(CommunityId communityId) {
        List<CommunityModerator> moderators =
                loadCommunityModeratorsPort.loadByCommunityId(communityId);
        log.debug("Loaded {} moderators for communityId={}", moderators.size(), communityId.stringify());
        return moderators;
    }

    @Override
    public List<CommunityModerator> getModerators(Community community) {
        return getModerators(community.communityId());
    }
}
