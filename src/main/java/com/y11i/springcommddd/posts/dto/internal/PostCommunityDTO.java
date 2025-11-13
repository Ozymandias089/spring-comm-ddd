package com.y11i.springcommddd.posts.dto.internal;

import lombok.Builder;
import lombok.Getter;

public final class PostCommunityDTO {
    @Getter private final String communityId;
    @Getter private final String communityName;
    @Getter private final String communityProfileImageUrl;

    @Builder
    public PostCommunityDTO(String communityId, String communityName, String communityProfileImageUrl) {
        this.communityId = communityId;
        this.communityName = communityName;
        this.communityProfileImageUrl = communityProfileImageUrl;
    }
}
