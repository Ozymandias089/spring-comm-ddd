package com.y11i.springcommddd.communities.application.port.out;

import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityStatus;

import java.util.List;

/**
 * 커뮤니티 목록 조회용 포트.
 */
public interface BrowseCommunitiesPort {
    /**
     * 주어진 상태의 커뮤니티를 페이지 단위로 조회합니다.
     *
     * @param status 필터링할 상태
     * @param page   0-based 페이지 번호
     * @param size   페이지 크기
     * @return 해당 페이지의 커뮤니티 목록
     */
    List<Community> loadByStatus(CommunityStatus status, int page, int size);

    /**
     * 주어진 상태의 커뮤니티 총 개수를 반환합니다.
     *
     * @param status 필터링할 상태
     * @return 전체 개수
     */
    long countByStatus(CommunityStatus status);

    List<Community> searchByStatusAndKeyword(CommunityStatus status, String keyword, int page, int size);
    long countByStatusAndKeyword(CommunityStatus status, String keyword);
}
