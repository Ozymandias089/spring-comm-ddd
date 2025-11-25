package com.y11i.springcommddd.communities.dto.internal;

import lombok.Builder;

/**
 * 커뮤니티 목록 조회 시 사용되는 요약 정보 DTO.
 *
 * <p>간단한 리스트 화면에서 사용되며, 상세 정보는 별도의
 * {@code GET /api/c/{nameKey}} API를 통해 조회합니다.</p>
 */
public record CommunitySummaryDTO(
        String communityId,
        String nameKey,
        String name,
        String profileImage // null 가능
) {
    @Builder
    public CommunitySummaryDTO{}
}
