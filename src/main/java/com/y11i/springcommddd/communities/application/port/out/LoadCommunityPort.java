package com.y11i.springcommddd.communities.application.port.out;

import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.CommunityName;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;

import java.util.Optional;

public interface LoadCommunityPort {
    Optional<Community> loadById(CommunityId communityId);
    Optional<Community> loadByNameKey(CommunityNameKey communityNameKey);
    Optional<Community> loadByName(CommunityName name);
}
