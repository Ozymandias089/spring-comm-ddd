package com.y11i.springcommddd.communities.dto.response;

import com.y11i.springcommddd.communities.dto.internal.CommunityRuleDTO;
import lombok.Builder;

import java.util.List;

public record CommunityRulesResponseDTO(
        String communityId,
        String communityName,
        String communityNameKey,
        List<CommunityRuleDTO> rules
) {
    @Builder
    public CommunityRulesResponseDTO{}
}
