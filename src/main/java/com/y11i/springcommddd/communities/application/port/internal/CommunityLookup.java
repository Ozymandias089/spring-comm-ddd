package com.y11i.springcommddd.communities.application.port.internal;

import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;

import java.util.List;

public interface CommunityLookup {
    Community getByIdOrThrow(CommunityId id);

    Community getByNameKeyOrThrow(CommunityNameKey nameKey);

    List<CommunityModerator> getModerators(CommunityId communityId);

    List<CommunityModerator> getModerators(Community community);
}
