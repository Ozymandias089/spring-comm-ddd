package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.dto.PostSummaryDTO;

import java.util.List;
import java.util.Optional;

/**
 * 목록/검색 유스케이스.
 */
public interface ListPostUseCase {
    PageResult<PostSummaryDTO> list(ListQuery listQuery);

    record ListQuery(
            Optional<CommunityId> communityId,
            Optional<MemberId> authorId,
            Optional<String> status,         // "PUBLISHED","DRAFT","ARCHIVED"
            int page, int size,
            Optional<String> sort // e.g. "createdAt,desc"
    ){}

    record PageResult<T>(int page, int size, long totalElements, int totalPages, List<T> content) {}
}
