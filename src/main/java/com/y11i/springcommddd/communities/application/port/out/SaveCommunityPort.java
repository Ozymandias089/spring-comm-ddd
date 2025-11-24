package com.y11i.springcommddd.communities.application.port.out;

import com.y11i.springcommddd.communities.domain.Community;

public interface SaveCommunityPort {
    Community save(Community community);
}
