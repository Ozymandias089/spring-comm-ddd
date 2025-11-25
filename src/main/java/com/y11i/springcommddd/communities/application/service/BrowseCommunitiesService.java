package com.y11i.springcommddd.communities.application.service;

import com.y11i.springcommddd.communities.application.port.in.BrowseCommunitiesUseCase;
import com.y11i.springcommddd.communities.application.port.internal.CommunityViewMapper;
import com.y11i.springcommddd.communities.application.port.out.BrowseCommunitiesPort;
import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityStatus;
import com.y11i.springcommddd.communities.dto.internal.CommunitySummaryDTO;
import com.y11i.springcommddd.communities.dto.response.CommunityPageResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrowseCommunitiesService implements BrowseCommunitiesUseCase {
    private final BrowseCommunitiesPort browseCommunitiesPort;
    private final CommunityViewMapper communityViewMapper;


    /**
     * 커뮤니티 목록을 페이지 단위로 조회합니다.
     *
     * @param query 페이지 번호, 크기, 상태 조건을 담은 쿼리
     * @return 페이지 응답 DTO
     */
    @Override
    public CommunityPageResponseDTO browseCommunities(BrowseCommunitiesQuery query) {
        int page = Math.max(query.page(), 0);
        int size = query.size() <= 0 ? 20 : query.size();
        CommunityStatus status = query.status() != null ? query.status() : CommunityStatus.ACTIVE;

        log.debug("Browsing communities: status={}, page={}, size={}", status, page, size);

        // 1. 목록 로드
        List<Community> communities = browseCommunitiesPort.loadByStatus(status, page, size);

        // 2. 카운트 로드
        long totalElements = browseCommunitiesPort.countByStatus(status);
        int totalPages = (int) ((totalElements + size - 1) / size);

        // 3. DTO 매핑
        List<CommunitySummaryDTO> content = communities.stream()
                .map(communityViewMapper::toSummary)
                .toList();

        log.debug("Browse communities result: {} items (totalElements={})", content.size(), totalElements);

        return CommunityPageResponseDTO.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();
    }
}
