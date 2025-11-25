package com.y11i.springcommddd.communities.application.port.internal;

import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityRule;
import com.y11i.springcommddd.communities.dto.internal.CommunityRuleDTO;
import com.y11i.springcommddd.communities.dto.internal.CommunitySummaryDTO;

public interface CommunityViewMapper {

    CommunitySummaryDTO toSummary(Community community);

    CommunityRuleDTO toRuleDTO(CommunityRule rule);

    CommunityRule toRuleEntity(CommunityRuleDTO dto);
}
