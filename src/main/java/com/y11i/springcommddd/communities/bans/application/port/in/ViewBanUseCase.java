package com.y11i.springcommddd.communities.bans.application.port.in;

import com.y11i.springcommddd.communities.bans.dto.response.CommunityBanPageResponseDTO;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.iam.domain.MemberId;

public interface ViewBanUseCase {
    /**
     * 특정 커뮤니티의 밴 목록을 조회합니다.
     * ADMIN 또는 해당 커뮤니티 MOD만 호출 가능하다고 가정합니다.
     */
    CommunityBanPageResponseDTO listCommunityBans(ListCommunityBansQuery query);

    CommunityBanPageResponseDTO listCommunityBanHistory(ListCommunityBanHistoryQuery query);

    // ───────────────── records ─────────────────

    /**
     * 커뮤니티별 밴 목록 조회 쿼리
     */
    record ListCommunityBansQuery(
            MemberId actorId,             // 권한 검증용
            CommunityNameKey nameKey,     // c/{nameKey}
            int page,
            int size
    ) {}

    record ListCommunityBanHistoryQuery(
            MemberId actorId,
            CommunityNameKey nameKey,
            int page,
            int size
    ) {}
}
