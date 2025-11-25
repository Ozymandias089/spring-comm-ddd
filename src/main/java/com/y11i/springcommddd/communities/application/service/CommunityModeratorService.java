package com.y11i.springcommddd.communities.application.service;

import com.y11i.springcommddd.communities.application.port.in.CommunityModeratorUseCase;
import com.y11i.springcommddd.communities.application.port.internal.CommunityAuthorization;
import com.y11i.springcommddd.communities.application.port.internal.CommunityLookup;
import com.y11i.springcommddd.communities.application.port.internal.CommunityModeratorViewMapper;
import com.y11i.springcommddd.communities.application.port.internal.CommunityViewMapper;
import com.y11i.springcommddd.communities.application.port.out.LoadCommunityModeratorsPort;
import com.y11i.springcommddd.communities.application.port.out.SaveCommunityModeratorsPort;
import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.dto.internal.CommunityModeratorDTO;
import com.y11i.springcommddd.communities.dto.internal.CommunitySummaryDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityModeratorsResponseDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityPageResponseDTO;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberRole;
import com.y11i.springcommddd.iam.domain.exception.UnauthorizedMemberAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommunityModeratorService implements CommunityModeratorUseCase {
    private final LoadCommunityModeratorsPort loadCommunityModeratorsPort;
    private final SaveCommunityModeratorsPort saveCommunityModeratorsPort;
    private final CommunityAuthorization communityAuthorization;
    private final CommunityLookup communityLookup;
    private final CommunityViewMapper communityViewMapper;
    private final CommunityModeratorViewMapper communityModeratorViewMapper;

    @Override
    public CommunityPageResponseDTO listMyModeratedCommunities(ListMyModeratedCommunitiesQuery query) {
        // 1. 멤버 존재 검증
        Member member = communityAuthorization.requireMember(query.actorId());
        log.debug("Listing community moderators for member id {}", member.memberId().stringify());
        // 2. 모더레이터 엔트리 전체조회
        List<CommunityModerator> moderatorEntries = loadCommunityModeratorsPort.loadByMemberId(member.memberId());

        // 3. Community JOIN → SummaryDTO 매핑
        List<CommunitySummaryDTO> content = moderatorEntries.stream()
                .map(CommunityModerator::communityId)
                .map(communityLookup::getByIdOrThrow)
                .filter(Objects::nonNull)
                .map(communityViewMapper::toSummary)
                .toList();

        int size = content.size();
        int totalPages = size == 0 ? 0 : 1;

        log.debug("Found {} moderated communities for member {}", size, query.actorId().stringify());

        return CommunityPageResponseDTO.builder()
                .content(content)
                .page(0)                  // 페이지네이션 안 쓰므로 0으로 고정
                .size(size)               // 현재 반환된 개수
                .totalElements(size)
                .totalPages(totalPages)
                .build();
    }

    @Override
    public CommunityModeratorsResponseDTO listModerators(CommunityNameKey nameKey) {
        // 1. 커뮤니티 조회 (nameKey -> Community)
        Community community = communityLookup.getByNameKeyOrThrow(nameKey);
        log.debug("Loaded Community c/{}", nameKey.value());

        // 2. 모더레이터 엔트리 조회
        List<CommunityModerator> moderators = communityLookup.getModerators(community);
        log.debug("Found {} moderators for c/{}", moderators.size(), nameKey.value());

        // 3. Member 로드해서 DTO 매핑
        List<CommunityModeratorDTO> moderatorDTOS = moderators.stream()
                .map(communityModeratorViewMapper::toDTO)
                .toList();

        // 4. 응답 DTO 빌드
        return CommunityModeratorsResponseDTO.builder()
                .communityId(community.communityId().stringify())
                .nameKey(community.nameKey().value())
                .moderators(moderatorDTOS)
                .build();
    }

    @Override
    @Transactional
    public void grantModerator(GrantModeratorCommand cmd) {
        // 1. 커뮤니티 & 모더레이터 로드
        Community community = communityLookup.getByNameKeyOrThrow(cmd.nameKey());
        List<CommunityModerator> moderators = communityLookup.getModerators(community);

        // 2. 액터 권한 검증 (ADMIN or MOD)
        Member actor = communityAuthorization.requireMember(cmd.actorId());
        communityAuthorization.requireAdminOrModerator(actor.memberId(), community.communityId());

        // 3. 타깃 멤버 검증 (ACTIVE && emailVerified)
        communityAuthorization.requireEligibleAsModerator(cmd.targetMemberId());

        // 4. 이미 모더인지 체크 (멱등성)
        boolean alreadyModerator = moderators.stream()
                .anyMatch(m -> m.memberId().equals(cmd.targetMemberId()));
        if (alreadyModerator) {
            log.info("Member {} is already moderator of c/{}",
                    cmd.targetMemberId().stringify(), cmd.nameKey().value());
            return;
        }

        // 5. 생성 + 저장
        CommunityModerator mod = CommunityModerator.grant(community.communityId(), cmd.targetMemberId());
        CommunityModerator saved = saveCommunityModeratorsPort.save(mod);

        log.info("Granted moderator {} for community c/{} by actor {}",
                saved.memberId().stringify(),
                cmd.nameKey().value(),
                actor.memberId().stringify());
    }

    @Override
    @Transactional
    public void revokeModerator(RevokeModeratorCommand cmd) {
        // 1. 커뮤니티 & 모더레이터 로드
        Community community = communityLookup.getByNameKeyOrThrow(cmd.nameKey());
        List<CommunityModerator> moderators = communityLookup.getModerators(community);

        // 2. 액터 권한 검증 (ADMIN or MOD)
        Member actor =communityAuthorization.requireMember(cmd.actorId());
        communityAuthorization.requireAdminOrModerator(actor.memberId(), community.communityId());

        // 3. 타깃이 실제 모더인지 확인
        CommunityModerator targetMod = moderators.stream()
                .filter(m -> m.memberId().equals(cmd.targetMemberId()))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Member {} is not a moderator of c/{}, cannot revoke",
                            cmd.targetMemberId().stringify(), cmd.nameKey().value());
                    return new UnauthorizedMemberAction("Target member is not a moderator of this community");
                });

        // (정책) 자기 자신 제거 허용 여부를 막고 싶다면 여기에서 체크 가능
         if (actor.memberId().equals(cmd.targetMemberId()) && !actor.hasRole(MemberRole.ADMIN)) throw new UnauthorizedMemberAction("You Cannot revoke yourself from the Moderator");

        // 4. 삭제
        saveCommunityModeratorsPort.delete(targetMod);

        log.info("Revoked moderator {} for community c/{} by actor {}",
                cmd.targetMemberId().stringify(),
                cmd.nameKey().value(),
                actor.memberId().stringify());
    }
}
