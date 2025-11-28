package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.dto.internal.PageResultDTO;
import com.y11i.springcommddd.posts.dto.response.PostSummaryResponseDTO;

public interface SearchCommunityPostsUseCase {
    record Query(
            String nameKey,
            MemberId viewerId,
            String keyword,
            String sort,
            int page,
            int size
    ){}

    PageResultDTO<PostSummaryResponseDTO> search(Query q);
}
