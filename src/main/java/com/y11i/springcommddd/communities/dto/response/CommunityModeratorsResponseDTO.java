package com.y11i.springcommddd.communities.dto.response;

import com.y11i.springcommddd.communities.dto.internal.CommunityModeratorDTO;
import lombok.Builder;

import java.util.List;

public record CommunityModeratorsResponseDTO(
        String communityId,
        String nameKey,
        List<CommunityModeratorDTO> moderators
) {
    @Builder
    public CommunityModeratorsResponseDTO{}
}
