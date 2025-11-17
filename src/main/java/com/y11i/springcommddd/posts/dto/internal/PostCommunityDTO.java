package com.y11i.springcommddd.posts.dto.internal;

import com.y11i.springcommddd.communities.domain.Community;
import lombok.Builder;

public record PostCommunityDTO(String communityId, String communityName, String communityProfileImageUrl) {
    @Builder
    public PostCommunityDTO {}

    public static PostCommunityDTO from(Community community) {
        return PostCommunityDTO.builder()
                .communityId(community.communityId().stringify())
                .communityName(community.communityName().value())
                .communityProfileImageUrl(community.profileImage().value())
                .build();
    }
}
