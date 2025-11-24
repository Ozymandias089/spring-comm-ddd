package com.y11i.springcommddd.communities.dto.response;

import java.time.Instant;

public record CommunityCreateResponseDTO(
        String communityId,
        String name,
        String nameKey,
        String status,
        Instant createdAt
) {
}
