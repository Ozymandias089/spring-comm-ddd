package com.y11i.springcommddd.communities.domain.exception;


public class CommunityNotFoundException extends RuntimeException {
    public CommunityNotFoundException(String communityId) {
        super("Community with id " + communityId + " not found");
    }
}
