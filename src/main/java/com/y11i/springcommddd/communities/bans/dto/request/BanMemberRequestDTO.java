package com.y11i.springcommddd.communities.bans.dto.request;

import lombok.Builder;

public record BanMemberRequestDTO(
        String targetMemberId,
        Long durationSeconds,   // null이면 영구 밴
        String reason
) {
    @Builder
    public BanMemberRequestDTO {}
}