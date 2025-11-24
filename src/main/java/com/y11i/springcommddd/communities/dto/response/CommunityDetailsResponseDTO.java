package com.y11i.springcommddd.communities.dto.response;

import com.y11i.springcommddd.communities.dto.internal.CommunityModeratorDTO;
import com.y11i.springcommddd.communities.dto.internal.CommunityRuleDTO;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

public record CommunityDetailsResponseDTO(
        String communityId,
        String communityProfileImage,
        String communityBannerImage,
        String communityName,
        String communityNameKey,
        String description,
        Instant activatedAt,
        String status,
        List<CommunityRuleDTO> rules,
        List<CommunityModeratorDTO> moderators
) {
    @Builder
    public CommunityDetailsResponseDTO {}
}
