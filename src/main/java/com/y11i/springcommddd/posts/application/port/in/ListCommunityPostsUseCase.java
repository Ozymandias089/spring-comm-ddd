package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.dto.internal.PageResultDTO;
import com.y11i.springcommddd.posts.dto.response.PostSummaryResponseDTO;

public interface ListCommunityPostsUseCase {
    record Query(
            String communityId,     // or CommunityId
            MemberId viewerId,
            String sort,            // "new", "top", "hot"
            int page,
            int size
    ) {}

    PageResultDTO<PostSummaryResponseDTO> listCommunityPosts(Query q);
}
