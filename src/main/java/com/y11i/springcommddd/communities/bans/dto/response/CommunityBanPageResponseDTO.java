package com.y11i.springcommddd.communities.bans.dto.response;

import com.y11i.springcommddd.communities.bans.dto.internal.CommunityBanSummaryDTO;
import lombok.Builder;

import java.util.List;

public record CommunityBanPageResponseDTO(
        List<CommunityBanSummaryDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    @Builder
    public CommunityBanPageResponseDTO{}
}
