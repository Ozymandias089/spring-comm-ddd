package com.y11i.springcommddd.communities.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;

public interface ManageCommunityStatusUseCase {
    CommunityId activateCommunity(ActivateCommunityCommand cmd);
    CommunityId archiveCommunity(ArchiveCommunityCommand cmd);
    CommunityId restoreCommunity(RestoreCommunityCommand cmd);

    record ActivateCommunityCommand(MemberId actorId, CommunityId communityId){}
    record ArchiveCommunityCommand(MemberId actorId, CommunityId communityId){}
    record RestoreCommunityCommand(MemberId actorId, CommunityId communityId){}
}
