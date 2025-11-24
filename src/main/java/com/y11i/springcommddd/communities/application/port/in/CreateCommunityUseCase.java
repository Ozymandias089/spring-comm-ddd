package com.y11i.springcommddd.communities.application.port.in;

import com.y11i.springcommddd.communities.dto.internal.CommunityRulesDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityCreateResponseDTO;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.List;

public interface CreateCommunityUseCase {

    CommunityCreateResponseDTO createCommunity(CreateCommunityCommand cmd);

    record CreateCommunityCommand(MemberId actorId, String name, String description, List<CommunityRulesDTO> rules){}
}
