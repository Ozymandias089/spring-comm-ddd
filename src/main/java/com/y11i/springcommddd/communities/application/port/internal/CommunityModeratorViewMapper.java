package com.y11i.springcommddd.communities.application.port.internal;

import com.y11i.springcommddd.communities.dto.internal.CommunityModeratorDTO;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;

public interface CommunityModeratorViewMapper {

    CommunityModeratorDTO toDTO(CommunityModerator moderator);
}
