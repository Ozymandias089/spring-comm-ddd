package com.y11i.springcommddd.communities.dto.request;

import com.y11i.springcommddd.communities.dto.internal.CommunityRulesDTO;

import java.util.List;

public record CommunityCreateRequestDTO(
        String name,
        String description,
        List<CommunityRulesDTO> rules
) {}
