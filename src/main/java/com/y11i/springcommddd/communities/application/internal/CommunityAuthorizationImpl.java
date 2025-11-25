package com.y11i.springcommddd.communities.application.internal;

import com.y11i.springcommddd.communities.application.port.internal.CommunityAuthorization;
import com.y11i.springcommddd.communities.application.port.out.LoadCommunityModeratorsPort;
import com.y11i.springcommddd.communities.application.port.out.LoadMemberForCommunityPort;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.MemberRole;
import com.y11i.springcommddd.iam.domain.MemberStatus;
import com.y11i.springcommddd.iam.domain.exception.MemberNotFound;
import com.y11i.springcommddd.iam.domain.exception.UnauthorizedMemberAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
class CommunityAuthorizationImpl implements CommunityAuthorization {

    private final LoadMemberForCommunityPort loadMemberForCommunityPort;
    private final LoadCommunityModeratorsPort loadCommunityModeratorsPort;

    @Override
    public Member requireMember(MemberId memberId) {
        return loadMemberForCommunityPort.loadById(memberId)
                .orElseThrow(() -> new MemberNotFound("Member not found"));
    }

    @Override
    public void requireActiveVerifiedMember(MemberId memberId) {
        Member member = requireMember(memberId);
        if (!member.emailVerified() || member.status() != MemberStatus.ACTIVE) {
            throw new UnauthorizedMemberAction("Member not active");
        }
    }

    @Override
    public void requireAdmin(MemberId memberId) {
        Member member = requireMember(memberId);
        if (!member.hasRole(MemberRole.ADMIN)) {
            throw new UnauthorizedMemberAction("Action not allowed");
        }
    }

    @Override
    public void requireAdminOrModerator(MemberId actorId, CommunityId communityId) {
        Member member = requireMember(actorId);

        if (member.hasRole(MemberRole.ADMIN)) return;

        List<CommunityModerator> moderators =
                loadCommunityModeratorsPort.loadByCommunityId(communityId);

        boolean isModerator = moderators.stream()
                .anyMatch(m -> m.memberId().equals(actorId));

        if (!isModerator) {
            log.warn("Member {} is neither admin nor moderator of {}", actorId.stringify(), communityId.stringify());
            throw new UnauthorizedMemberAction("This action is only allowed for admin or community moderators");
        }
    }

    @Override
    public void requireEligibleAsModerator(MemberId targetId) {
        Member target = requireMember(targetId);
        if (target.status() != MemberStatus.ACTIVE || !target.emailVerified()) {
            throw new UnauthorizedMemberAction("Target member is not active or email not verified");
        }
    }
}
