package com.y11i.springcommddd.comments.application.port.in;

import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.comments.dto.internal.CommentSummaryDTO;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.dto.internal.PageResultDTO;

public interface ListCommentUseCase {
    PageResultDTO<CommentSummaryDTO> listComment(Query q);

    record Query(
            PostId postId,
            CommentId parentCommentId, // nullable -> 루트/대댓글 구분
            MemberId viewerId,         // null이면 비로그인
            String sort,               // "new"만 먼저 지원해도 됨
            int page,
            int size
    ){}
}
