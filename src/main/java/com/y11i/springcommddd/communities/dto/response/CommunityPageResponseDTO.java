package com.y11i.springcommddd.communities.dto.response;

import com.y11i.springcommddd.communities.dto.internal.CommunitySummaryDTO;
import lombok.Builder;

import java.util.List;

public record CommunityPageResponseDTO(
        List<CommunitySummaryDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    @Builder
    public CommunityPageResponseDTO{}
}
