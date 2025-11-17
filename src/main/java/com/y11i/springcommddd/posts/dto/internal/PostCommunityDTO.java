package com.y11i.springcommddd.posts.dto.internal;

import lombok.Builder;

public record PostCommunityDTO(String communityId, String communityName, String communityProfileImageUrl) {
    @Builder
    public PostCommunityDTO {}
}
