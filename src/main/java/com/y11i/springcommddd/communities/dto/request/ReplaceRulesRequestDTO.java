package com.y11i.springcommddd.communities.dto.request;

import com.y11i.springcommddd.communities.dto.internal.CommunityRuleDTO;

import java.util.List;

public record ReplaceRulesRequestDTO(List<CommunityRuleDTO> rules) {
}
