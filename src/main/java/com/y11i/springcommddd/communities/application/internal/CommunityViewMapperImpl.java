package com.y11i.springcommddd.communities.application.internal;

import com.y11i.springcommddd.communities.application.port.internal.CommunityViewMapper;
import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityRule;
import com.y11i.springcommddd.communities.dto.internal.CommunityRuleDTO;
import com.y11i.springcommddd.communities.dto.internal.CommunitySummaryDTO;
import com.y11i.springcommddd.shared.domain.ImageUrl;
import org.springframework.stereotype.Component;

@Component
class CommunityViewMapperImpl implements CommunityViewMapper {

    @Override
    public CommunitySummaryDTO toSummary(Community community) {
        String profileImageUrl = null;
        ImageUrl profile = community.profileImage();
        if (profile != null) {
            profileImageUrl = profile.value();
        }

        return CommunitySummaryDTO.builder()
                .communityId(community.communityId().stringify())
                .nameKey(community.nameKey().value())
                .name(community.communityName().value())
                .profileImage(profileImageUrl)
                .build();
    }

    @Override
    public CommunityRuleDTO toRuleDTO(CommunityRule rule) {
        return CommunityRuleDTO.builder()
                .title(rule.title())
                .description(rule.description())
                .displayOrder(rule.displayOrder())
                .build();
    }

    @Override
    public CommunityRule toRuleEntity(CommunityRuleDTO dto) {
        return new CommunityRule(dto.title(), dto.description(), dto.displayOrder());
    }
}
