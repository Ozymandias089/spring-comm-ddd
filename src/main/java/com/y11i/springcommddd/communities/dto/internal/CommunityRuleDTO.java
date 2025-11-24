package com.y11i.springcommddd.communities.dto.internal;

import lombok.Builder;

public record CommunityRuleDTO(String title, String description, int displayOrder) {
    @Builder
    public CommunityRuleDTO {}
}
