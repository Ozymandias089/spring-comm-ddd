package com.y11i.springcommddd.communities.application.service;

import com.y11i.springcommddd.communities.application.port.in.GetCommunityDetailsUseCase;
import com.y11i.springcommddd.communities.application.port.internal.CommunityLookup;
import com.y11i.springcommddd.communities.application.port.internal.CommunityModeratorViewMapper;
import com.y11i.springcommddd.communities.application.port.internal.CommunityViewMapper;
import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.dto.internal.CommunityModeratorDTO;
import com.y11i.springcommddd.communities.dto.internal.CommunityRuleDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityDetailsResponseDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityRulesResponseDTO;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;
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
    private final CommunityLookup communityLookup;
    private final CommunityViewMapper communityViewMapper;
    private final CommunityModeratorViewMapper communityModeratorViewMapper;

    /**
     * 네임키를 기준으로 커뮤니티를 검색해 정보를 반환합니다.
     * @param communityNameKey 검색의 기준이 될 네임키 객체
     * @return 커뮤니티의 상세 정보.
     */
    @Override
    public CommunityDetailsResponseDTO getCommunityDetails(CommunityNameKey communityNameKey) {
        // 1. 커뮤니티 로드
        Community community = communityLookup.getByNameKeyOrThrow(communityNameKey);
        log.debug("Get community details for c/{}", communityNameKey.value());
        // 2. 규칙 매핑
        List<CommunityRuleDTO> ruleDTOs = community.rules().stream().map(communityViewMapper::toRuleDTO).toList();
        log.debug("Mapped community rules for c/{}", communityNameKey.value());
        // 3. 모더레이터 엔트리 조회
        List<CommunityModerator> moderators = communityLookup.getModerators(community);
        log.debug("Get community moderators for c/{}", communityNameKey.value());
        // 4. 모더레이터 DTO 매핑
        List<CommunityModeratorDTO> moderatorDTOs = moderators.stream().map(communityModeratorViewMapper::toDTO).toList();
        log.debug("Mapped community moderators for c/{}", communityNameKey.value());
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

    /**
     * 커뮤니티의 규칙만 찾아 반환합니다.
     * @param cmd 커맨드 객체
     * @return 규칙만 담긴 객체
     */
    @Override
    public CommunityRulesResponseDTO getRules(GetRulesCommand cmd) {
        // 1. Load Community
        Community community = communityLookup.getByNameKeyOrThrow(cmd.nameKey());
        log.debug("Get community rules for {}", cmd.nameKey().value());
        // 2. Map rules as List of CommunityRuleDTO
        List<CommunityRuleDTO> rules = community.rules().stream().map(communityViewMapper::toRuleDTO).toList();
        log.debug("Mapped community rules for {}. total {} items.", cmd.nameKey().value(), rules.size());
        // 3. Map and return rules and basic info as responseDTO
        return CommunityRulesResponseDTO.builder()
                .communityId(community.communityId().stringify())
                .communityName(community.communityName().value())
                .communityNameKey(community.nameKey().value())
                .rules(rules)
                .build();
    }
}
