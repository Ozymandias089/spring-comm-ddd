package com.y11i.springcommddd.communities.application.internal;

import com.y11i.springcommddd.communities.application.port.internal.CommunityModeratorViewMapper;
import com.y11i.springcommddd.communities.application.port.out.LoadMemberForCommunityPort;
import com.y11i.springcommddd.communities.dto.internal.CommunityModeratorDTO;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.exception.MemberNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CommunityModeratorViewMapperImpl implements CommunityModeratorViewMapper {

    private final LoadMemberForCommunityPort loadMemberForCommunityPort;

    @Override
    public CommunityModeratorDTO toDTO(CommunityModerator moderator) {
        Member member = loadMemberForCommunityPort.loadById(moderator.memberId())
                .orElseThrow(() -> new MemberNotFound("Moderator member not found"));

        return CommunityModeratorDTO.builder()
                .memberId(moderator.memberId().stringify())
                .displayName(member.displayName().value())
                .profileImage(
                        member.profileImage() != null
                                ? member.profileImage().value()
                                : null
                )
                .build();
    }
}
