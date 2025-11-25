package com.y11i.springcommddd.communities.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityStatus;
import com.y11i.springcommddd.communities.dto.response.CommunityPageResponseDTO;

/**
 * 활성(또는 지정한 상태의) 커뮤니티 목록을 페이지 단위로 조회하는 유스케이스.
 */
public interface BrowseCommunitiesUseCase {
    /**
     * 커뮤니티 목록을 페이지 단위로 조회합니다.
     *
     * @param query 페이지 번호, 크기, 상태 조건을 담은 쿼리
     * @return 페이지 응답 DTO
     */
    CommunityPageResponseDTO browseCommunities(BrowseCommunitiesQuery query);

    /**
     * 커뮤니티 목록 조회 쿼리.
     *
     * @param page   0-based 페이지 번호
     * @param size   페이지 크기
     * @param status 필터링할 커뮤니티 상태(기본 ACTIVE)
     */
    record BrowseCommunitiesQuery(
            int page,
            int size,
            CommunityStatus status
    ) {}
}
