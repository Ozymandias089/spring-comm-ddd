package com.y11i.springcommddd.communities.application.service;

import com.y11i.springcommddd.communities.application.port.in.GetCommunityDetailsUseCase;
import com.y11i.springcommddd.communities.application.port.out.LoadCommunityModeratorsPort;
import com.y11i.springcommddd.communities.application.port.out.LoadCommunityPort;
import com.y11i.springcommddd.communities.application.port.out.LoadMemberForCommunityPort;
import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.domain.CommunityRule;
import com.y11i.springcommddd.communities.domain.exception.CommunityNotFound;
import com.y11i.springcommddd.communities.dto.internal.CommunityModeratorDTO;
import com.y11i.springcommddd.communities.dto.internal.CommunityRuleDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityDetailsResponseDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityRulesResponseDTO;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.exception.MemberNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetCommunityDetailsService implements GetCommunityDetailsUseCase {
    private final LoadCommunityPort loadCommunityPort;
    private final LoadCommunityModeratorsPort loadCommunityModeratorsPort;
    private final LoadMemberForCommunityPort loadMemberForCommunityPort;


    @Override
    public CommunityDetailsResponseDTO getCommunityDetails(CommunityNameKey communityNameKey) {
        // 1. 커뮤니티 로드
        Community community = loadCommunityPort.loadByNameKey(communityNameKey).orElseThrow(() -> new CommunityNotFound("Community not found"));
        log.debug("Get community details for {}", communityNameKey);
        // 2. 규칙 매핑
        List<CommunityRuleDTO> ruleDTOs = community.rules().stream().map(this::toCommunityRuleDTO).toList();
        log.debug("Mapped community rules for {}", communityNameKey);
        // 3. 모더레이터 엔트리 조회
        List<CommunityModerator> moderators = loadCommunityModeratorsPort.loadByCommunityId(community.communityId());
        log.debug("Get community moderators for {}", communityNameKey);
        // 4. 모더레이터 DTO 매핑
        List<CommunityModeratorDTO> moderatorDTOs = moderators.stream().map(this::toCommunityModeratorDTO).toList();
        log.debug("Mapped community moderators for {}", communityNameKey);
        // 5. DTO 빌드
        return CommunityDetailsResponseDTO.builder()
                .communityId(community.communityId().stringify())
                .communityProfileImage(
                        community.profileImage() != null ? community.profileImage().value() : null
                )
                .communityBannerImage(
                        community.bannerImage() != null ? community.bannerImage().value() : null
                )
                .communityName(community.communityName().value())
                .communityNameKey(community.nameKey().value())
                .description(
                        community.communityDescription() != null
                                ? community.communityDescription().value()
                                : null
                )
                .activatedAt(community.activatedAt())
                .status(community.status().name())
                .rules(ruleDTOs)
                .moderators(moderatorDTOs)
                .build();
    }

    @Override
    public CommunityRulesResponseDTO getRules(GetRulesCommand cmd) {
        // 1. Load Community
        Community community = loadCommunityPort.loadByNameKey(cmd.nameKey()).orElseThrow(() -> new CommunityNotFound("Community not found"));
        log.debug("Get community rules for {}", cmd.nameKey());
        // 2. Map rules as List of CommunityRuleDTO
        List<CommunityRuleDTO> rules = community.rules().stream().map(this::toCommunityRuleDTO).toList();
        log.debug("Mapped community rules for {}. total {} items.", cmd.nameKey(), rules.size());
        // 3. Map and return rules and basic info as responseDTO
        return CommunityRulesResponseDTO.builder()
                .communityId(community.communityId().stringify())
                .communityName(community.communityName().value())
                .communityNameKey(community.nameKey().value())
                .rules(rules)
                .build();
    }

    private CommunityRuleDTO toCommunityRuleDTO(CommunityRule rule) {
        return CommunityRuleDTO.builder()
                .title(rule.title())
                .description(rule.description())
                .displayOrder(rule.displayOrder())
                .build();
    }

    private CommunityModeratorDTO toCommunityModeratorDTO(CommunityModerator moderator) {
        Member member = loadMemberForCommunityPort.loadById(moderator.memberId()).orElseThrow(() -> new MemberNotFound("Moderator member not found"));

        return CommunityModeratorDTO.builder()
                .memberId(moderator.memberId().stringify())
                .displayName(member.displayName().value())
                .profileImage(member.profileImage() != null ? member.profileImage().value() : null)
                .build();
    }
}
