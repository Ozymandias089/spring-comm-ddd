package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.dto.internal.PageResultDTO;
import com.y11i.springcommddd.posts.dto.response.PostSummaryResponseDTO;
import jakarta.annotation.Nullable;

public interface ListHomeFeedPostsUseCase {
    record Query(
            @Nullable MemberId viewerId,      // 비로그인 허용이면 @Nullable
            String sort,            // "new", "top", "hot" 등
            int page,
            int size
    ) {}

    PageResultDTO<PostSummaryResponseDTO> listHomeFeed(Query q);
}
