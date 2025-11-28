package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.dto.internal.PageResultDTO;
import com.y11i.springcommddd.posts.dto.response.PostSummaryResponseDTO;
import jakarta.annotation.Nullable;

public interface SearchHomePostsUseCase {
    record Query(
            @Nullable MemberId viewerId,
            String keyword,
            String sort,
            int page,
            int size
    ){}

    PageResultDTO<PostSummaryResponseDTO> search(Query q);
}
