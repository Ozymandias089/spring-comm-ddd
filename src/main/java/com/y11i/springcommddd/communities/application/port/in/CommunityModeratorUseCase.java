package com.y11i.springcommddd.communities.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.dto.response.CommunityModeratorsResponseDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityPageResponseDTO;
import com.y11i.springcommddd.iam.domain.MemberId;


public interface CommunityModeratorUseCase {
    // 2-7: 내가 모더레이터인 커뮤니티 목록
    CommunityPageResponseDTO listMyModeratedCommunities(ListMyModeratedCommunitiesQuery query);

    // 3-1: 해당 커뮤니티의 모더레이터 목록
    CommunityModeratorsResponseDTO listModerators(CommunityNameKey nameKey);

    // 3-2: 모더레이터 부여
    void grantModerator(GrantModeratorCommand cmd);

    // 3-3: 모더레이터 박탈
    void revokeModerator(RevokeModeratorCommand cmd);


    // --- DTO/Command/Query 타입들 ---

    record ListMyModeratedCommunitiesQuery(
            MemberId actorId,
            int page,
            int size
    ) {}

    record GrantModeratorCommand(
            MemberId actorId,          // 권한 체크용 (ADMIN or 기존 MOD)
            CommunityNameKey nameKey,  // 대상 커뮤니티
            MemberId targetMemberId    // 모더 부여 대상
    ) {}

    record RevokeModeratorCommand(
            MemberId actorId,          // 권한 체크용
            CommunityNameKey nameKey,  // 대상 커뮤니티
            MemberId targetMemberId    // 모더 박탈 대상
    ) {}
}
