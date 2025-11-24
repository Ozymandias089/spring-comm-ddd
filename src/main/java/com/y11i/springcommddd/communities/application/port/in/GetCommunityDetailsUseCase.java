package com.y11i.springcommddd.communities.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.dto.response.CommunityDetailsResponseDTO;

public interface GetCommunityDetailsUseCase {
    CommunityDetailsResponseDTO getCommunityDetails(CommunityNameKey communityNameKey);
}
