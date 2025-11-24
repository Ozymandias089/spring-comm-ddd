package com.y11i.springcommddd.communities.dto.internal;

import lombok.Builder;

public record CommunityModeratorDTO(
        String memberId,
        String displayName,
        String profileImage
) {
    @Builder
    public CommunityModeratorDTO {}
}
