package com.y11i.springcommddd.communities.domain.exception;

import com.y11i.springcommddd.communities.domain.CommunityId;

public class CommunityNotFoundException extends RuntimeException {
    public CommunityNotFoundException(CommunityId communityId) {
        super("Community with id " + communityId.id().toString() + " not found");
    }
}
