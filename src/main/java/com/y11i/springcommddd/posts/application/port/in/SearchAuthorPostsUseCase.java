package com.y11i.springcommddd.posts.application.port.in;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.dto.internal.PageResultDTO;
import com.y11i.springcommddd.posts.dto.response.PostSummaryResponseDTO;
import jakarta.annotation.Nullable;

public interface SearchAuthorPostsUseCase {

    record Query(
            MemberId authorId,           // whose posts to search
            @Nullable MemberId viewerId, // 현재 로그인 유저 (myVote 계산용, 비로그인 허용)
            @Nullable String keyword,    // null/blank면 키워드 없이 전체
            String sort,                 // "new", "top" ...
            int page,
            int size
    ) {}

    PageResultDTO<PostSummaryResponseDTO> searchByAuthor(Query q);
}
