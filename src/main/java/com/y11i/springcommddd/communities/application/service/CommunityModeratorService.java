package com.y11i.springcommddd.communities.application.service;

import com.y11i.springcommddd.communities.application.port.in.CommunityModeratorUseCase;
import com.y11i.springcommddd.communities.application.port.out.LoadCommunityModeratorsPort;
import com.y11i.springcommddd.communities.application.port.out.LoadCommunityPort;
import com.y11i.springcommddd.communities.application.port.out.LoadMemberForCommunityPort;
import com.y11i.springcommddd.communities.application.port.out.SaveCommunityModeratorsPort;
import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.domain.exception.CommunityNotFound;
import com.y11i.springcommddd.communities.dto.internal.CommunityModeratorDTO;
import com.y11i.springcommddd.communities.dto.internal.CommunitySummaryDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityModeratorsResponseDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityPageResponseDTO;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.MemberRole;
import com.y11i.springcommddd.iam.domain.MemberStatus;
import com.y11i.springcommddd.iam.domain.exception.MemberNotFound;
import com.y11i.springcommddd.iam.domain.exception.UnauthorizedMemberAction;
import com.y11i.springcommddd.shared.domain.ImageUrl;
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
    private final LoadMemberForCommunityPort loadMemberForCommunityPort;
    private final LoadCommunityPort loadCommunityPort;

    @Override
    public CommunityPageResponseDTO listMyModeratedCommunities(ListMyModeratedCommunitiesQuery query) {
        // 1. 멤버 존재 검증
        Member member = loadMemberForCommunityPort.loadById(query.actorId()).orElseThrow(() -> new MemberNotFound("Member not found"));
        log.debug("Listing community moderators for member id {}", member.memberId().stringify());
        // 2. 모더레이터 엔트리 전체조회
        List<CommunityModerator> moderatorEntries = loadCommunityModeratorsPort.loadByMemberId(member.memberId());

        // 3. Community JOIN
        List<CommunitySummaryDTO> content = moderatorEntries.stream()
                .map(CommunityModerator::communityId)
                .map(cid -> loadCommunityPort.loadById(cid).orElse(null))
                .filter(Objects::nonNull)
                .map(this::toSummaryDTO)
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
        Community community = loadCommunityPort.loadByNameKey(nameKey)
                .orElseThrow(() -> new CommunityNotFound("Community Not Found"));
        log.debug("Loaded Community c/{}", nameKey.value());

        // 2. 모더레이터 엔트리 조회
        List<CommunityModerator> moderators =
                loadCommunityModeratorsPort.loadByCommunityId(community.communityId());
        //noinspection LoggingSimilarMessage
        log.debug("Found {} moderators for c/{}", moderators.size(), nameKey.value());

        // 3. Member 로드해서 DTO 매핑
        List<CommunityModeratorDTO> moderatorDTOS = moderators.stream()
                .map(this::toCommunityModeratorDTO)
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
        Community community = loadCommunityByNameKey(cmd.nameKey());
        List<CommunityModerator> moderators = loadModeratorsFor(community);

        // 2. 액터 권한 검증 (ADMIN or MOD)
        Member actor = loadMemberOrThrow(cmd.actorId());
        ensureActorCanManageModerators(actor, moderators, cmd.nameKey());

        // 3. 타깃 멤버 검증 (ACTIVE && emailVerified)
        Member target = loadMemberOrThrow(cmd.targetMemberId());
        ensureEligibleAsModerator(target);

        // 4. 이미 모더인지 체크 (멱등성)
        boolean alreadyModerator = moderators.stream()
                .anyMatch(m -> m.memberId().equals(cmd.targetMemberId()));
        if (alreadyModerator) {
            log.info("Member {} is already moderator of c/{}",
                    cmd.targetMemberId().stringify(), cmd.nameKey().value());
            return;
        }

        // 5. 생성 + 저장
        CommunityModerator mod = CommunityModerator.grant(community.communityId(), target.memberId());
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
        Community community = loadCommunityByNameKey(cmd.nameKey());
        List<CommunityModerator> moderators = loadModeratorsFor(community);

        // 2. 액터 권한 검증 (ADMIN or MOD)
        Member actor = loadMemberOrThrow(cmd.actorId());
        ensureActorCanManageModerators(actor, moderators, cmd.nameKey());

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
    // --- 헬퍼 메서드들 ---

    private Community loadCommunityByNameKey(CommunityNameKey nameKey) {
        Community community = loadCommunityPort.loadByNameKey(nameKey)
                .orElseThrow(() -> new CommunityNotFound("Community Not Found"));
        log.debug("Loaded Community c/{}", nameKey.value());
        return community;
    }

    private List<CommunityModerator> loadModeratorsFor(Community community) {
        List<CommunityModerator> moderators =
                loadCommunityModeratorsPort.loadByCommunityId(community.communityId());
        log.debug("Found {} moderators for c/{}", moderators.size(), community.nameKey().value());
        return moderators;
    }

    private Member loadMemberOrThrow(MemberId memberId) {
        return loadMemberForCommunityPort.loadById(memberId)
                .orElseThrow(() -> new MemberNotFound("Member not found"));
    }

    /**
     * 액터가 ADMIN이거나, 해당 커뮤니티의 모더레이터 중 한 명인지 검증.
     */
    private void ensureActorCanManageModerators(
            Member actor,
            List<CommunityModerator> moderators,
            CommunityNameKey communityNameKey
    ) {
        boolean isModerator = moderators.stream()
                .anyMatch(m -> m.memberId().equals(actor.memberId()));

        if (!(actor.hasRole(MemberRole.ADMIN) || isModerator)) {
            log.warn("Member {} attempted to manage moderators in c/{} without permission",
                    actor.memberId().stringify(), communityNameKey.value());
            throw new UnauthorizedMemberAction("This action is only allowed for admin or existing community moderators");
        }
    }

    /**
     * 모더로 부여 가능한 상태인지 검증 (ACTIVE && emailVerified)
     */
    private void ensureEligibleAsModerator(Member target) {
        if (target.status() != MemberStatus.ACTIVE || !target.emailVerified()) {
            log.warn("Target member {} is not eligible to become moderator (status={}, emailVerified={})",
                    target.memberId().stringify(), target.status(), target.emailVerified());
            throw new UnauthorizedMemberAction("Target member is not active or email not verified");
        }
    }

    private CommunitySummaryDTO toSummaryDTO(Community community) {
        String profileImageUrl = null;
        ImageUrl profile = community.profileImage();
        if (profile != null) {
            profileImageUrl = profile.value(); // ImageUrl의 getter 이름에 맞게 value()/url() 등으로 수정
        }

        return CommunitySummaryDTO.builder()
                .communityId(community.communityId().stringify())
                .nameKey(community.nameKey().value())
                .name(community.communityName().value())
                .profileImage(profileImageUrl)
                .build();
    }

    private CommunityModeratorDTO toCommunityModeratorDTO(CommunityModerator moderator) {
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
