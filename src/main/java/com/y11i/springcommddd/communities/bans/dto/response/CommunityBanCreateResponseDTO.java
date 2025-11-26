package com.y11i.springcommddd.communities.bans.dto.response;

import lombok.Builder;

public record CommunityBanCreateResponseDTO(
        String banId
) {
    @Builder
    public CommunityBanCreateResponseDTO {}
}