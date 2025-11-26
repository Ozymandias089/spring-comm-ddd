package com.y11i.springcommddd.communities.bans.application.service;

import com.y11i.springcommddd.communities.application.port.internal.CommunityAuthorization;
import com.y11i.springcommddd.communities.application.port.internal.CommunityLookup;
import com.y11i.springcommddd.communities.bans.application.port.in.ViewBanUseCase;
import com.y11i.springcommddd.communities.bans.application.port.out.LoadBanPort;
import com.y11i.springcommddd.communities.bans.domain.CommunityBan;
import com.y11i.springcommddd.communities.bans.dto.internal.CommunityBanSummaryDTO;
import com.y11i.springcommddd.communities.bans.dto.response.CommunityBanPageResponseDTO;
import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ViewBanService implements ViewBanUseCase {
    private final CommunityLookup communityLookup;
    private final CommunityAuthorization communityAuthorization;
    private final LoadBanPort loadBanPort;

    // ───────────────────────────────── 조회(use case) ─────────────────────────────────

    @Override
    public CommunityBanPageResponseDTO listCommunityBans(ListCommunityBansQuery query) {
        // 1. 커뮤니티 + 권한 검증
        Community community = loadCommunityAndAuthorize(query.nameKey().value(), query.actorId());
        log.debug("Listing ACTIVE bans for community c/{}", community.nameKey().value());

        // 2. 전체 밴 목록 로드 후 활성 밴만 필터링
        List<CommunityBan> allBans = loadBanPort.findBansByCommunityId(community.communityId());
        List<CommunityBan> activeBans = allBans.stream()
                .filter(CommunityBan::isActive)
                .toList();

        // 3. 공통 페이징 + DTO 매핑
        CommunityBanPageResponseDTO result =
                toPageResponse(activeBans, community, query.page(), query.size());

        log.debug("Listed {} ACTIVE bans (page={}, size={}, totalElements={})",
                result.content().size(), result.page(), result.size(), result.totalElements());

        return result;
    }

    @Override
    public CommunityBanPageResponseDTO listCommunityBanHistory(ListCommunityBanHistoryQuery query) {
        // 1. 커뮤니티 + 권한 검증
        Community community = loadCommunityAndAuthorize(query.nameKey().value(), query.actorId());
        log.debug("Listing ALL ban history for community c/{}", community.nameKey().value());

        // 2. 전체 밴 목록 로드
        List<CommunityBan> allBans = loadBanPort.findBansByCommunityId(community.communityId());

        // 3. 공통 페이징 + DTO 매핑
        CommunityBanPageResponseDTO result =
                toPageResponse(allBans, community, query.page(), query.size());

        log.debug("Listed {} TOTAL bans (page={}, size={}, totalElements={})",
                result.content().size(), result.page(), result.size(), result.totalElements());

        return result;
    }

    // ──────────────────────────────── private helpers ────────────────────────────────

    /**
     * nameKey로 커뮤니티를 로드하고, ADMIN 또는 해당 커뮤니티 MOD 권한을 요구한다.
     * (조회/명령 양쪽에서 공통으로 사용)
     */
    private Community loadCommunityAndAuthorize(String nameKey, MemberId actorId) {
        Community community = communityLookup.getByNameKeyOrThrow(new CommunityNameKey(nameKey));
        communityAuthorization.requireAdminOrModerator(actorId, community.communityId());
        return community;
    }

    /**
     * 인메모리 List<CommunityBan>을 페이지네이션하여 CommunityBanPageResponseDTO로 변환.
     * 나중에 DB 레벨 페이징으로 바꿀 때 이 메서드만 교체하면 된다.
     */
    private CommunityBanPageResponseDTO toPageResponse(
            List<CommunityBan> bans,
            Community community,
            int requestedPage,
            int requestedSize
    ) {
        int page = Math.max(requestedPage, 0);
        int size = requestedSize <= 0 ? 20 : requestedSize;

        long totalElements = bans.size();
        int totalPages = totalElements == 0
                ? 0
                : (int) ((totalElements + size - 1) / size);

        int fromIndex = page * size;
        if (fromIndex >= totalElements) {
            return CommunityBanPageResponseDTO.builder()
                    .content(List.of())
                    .page(page)
                    .size(size)
                    .totalElements(totalElements)
                    .totalPages(totalPages)
                    .build();
        }

        int toIndex = (int) Math.min(fromIndex + size, totalElements);
        List<CommunityBan> pageContent = bans.subList(fromIndex, toIndex);

        List<CommunityBanSummaryDTO> content = pageContent.stream()
                .map(ban -> toSummaryDTO(ban, community))
                .toList();

        return CommunityBanPageResponseDTO.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }

    /**
     * 도메인 객체 → 요약 DTO 매핑
     * (조회 use case 간 공유)
     */
    private CommunityBanSummaryDTO toSummaryDTO(CommunityBan ban, Community community) {
        return CommunityBanSummaryDTO.builder()
                .banId(ban.banId().stringify())
                .communityId(ban.communityId().stringify())
                .communityNameKey(community.nameKey().value())
                .bannedMemberId(ban.bannedMemberId().stringify())
                .bannedMemberDisplayName(null)   // TODO: Member 쪽 조회/조인 들어가면 채워넣기
                .processorId(ban.processorId().stringify())
                .processorDisplayName(null)      // TODO: 상동
                .reason(ban.reason().value())
                .bannedAt(ban.bannedAt())
                .expiresAt(ban.expiresAt())
                .liftsAt(ban.liftedAt())
                .active(ban.isActive())
                .build();
    }
}
