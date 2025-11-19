package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.dto.internal.PageResultDTO;
import com.y11i.springcommddd.posts.dto.response.PostSummaryResponseDTO;

public interface ListDraftsUseCase {
    record Query(
            MemberId memberId,
            String sort,
            int page,
            int size
    ){}

    PageResultDTO<PostSummaryResponseDTO> listDrafts(Query q);
}
