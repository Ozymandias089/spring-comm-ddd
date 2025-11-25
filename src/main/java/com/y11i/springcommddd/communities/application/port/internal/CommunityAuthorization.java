package com.y11i.springcommddd.communities.application.port.internal;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;

public interface CommunityAuthorization {

    Member requireMember(MemberId memberId);

    void requireActiveVerifiedMember(MemberId memberId);

    void requireAdmin(MemberId memberId);

    void requireAdminOrModerator(MemberId actorId, CommunityId communityId);

    void requireEligibleAsModerator(MemberId targetId);
}
