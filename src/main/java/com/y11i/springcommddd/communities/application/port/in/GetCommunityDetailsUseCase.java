package com.y11i.springcommddd.communities.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.dto.response.CommunityDetailsResponseDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityRulesResponseDTO;

public interface GetCommunityDetailsUseCase {
    CommunityDetailsResponseDTO getCommunityDetails(CommunityNameKey communityNameKey);
    CommunityRulesResponseDTO getRules(GetRulesCommand cmd);

    record GetRulesCommand(CommunityNameKey nameKey){}
}
