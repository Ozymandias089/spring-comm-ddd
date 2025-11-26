package com.y11i.springcommddd.communities.bans.dto.internal;

import lombok.Builder;

import java.time.Instant;

public record CommunityBanSummaryDTO(
        String banId,
        String communityId,
        String communityNameKey,
        String bannedMemberId,
        String bannedMemberDisplayName,
        String processorId,
        String processorDisplayName,
        String reason,
        Instant bannedAt,
        Instant expiresAt,
        Instant liftsAt,
        boolean active
) {
    @Builder
    public CommunityBanSummaryDTO{}
}