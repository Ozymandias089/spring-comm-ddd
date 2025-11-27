package com.y11i.springcommddd.comments.api;

import com.y11i.springcommddd.comments.application.port.in.ListCommentUseCase;
import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.comments.dto.internal.CommentSummaryDTO;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.posts.dto.internal.PageResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.y11i.springcommddd.posts.api.support.CurrentMemberResolver.resolveCurrentMemberIdOrNull;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class ListCommentController {
    private final ListCommentUseCase listCommentUseCase;

    /**
     * 댓글 목록 조회 (루트 + 대댓글 공통).
     *
     * <p>
     * - GET /api/posts/{postId}/comments<br>
     * - parentId 파라미터가 없으면 루트 댓글 목록<br>
     * - parentId 파라미터가 있으면 해당 댓글의 대댓글 목록
     * </p>
     *
     * 예)
     * <ul>
     *   <li>루트 댓글: GET /api/posts/{postId}/comments?sort=new&page=0&size=20</li>
     *   <li>대댓글:   GET /api/posts/{postId}/comments?parentId={commentId}&sort=new&page=0&size=20</li>
     * </ul>
     */
    @GetMapping(path = "/{postId}/comments", produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public PageResultDTO<CommentSummaryDTO> listComments(
            @PathVariable("postId") String postId,
            @RequestParam(value = "parentId", required = false) String parentIdRaw,
            @RequestParam(name = "sort", defaultValue = "new") String sort,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        // 로그인 유저가 있으면 현재 유저 ID, 없으면 null (홈 피드 로직과 동일 패턴)
        MemberId viewerId = resolveCurrentMemberIdOrNull();

        // parentId 가 있으면 대댓글, 없으면 루트 댓글
        var parentId = (parentIdRaw != null && !parentIdRaw.isBlank())
                ? CommentId.objectify(parentIdRaw)
                : null;

        var query = new ListCommentUseCase.Query(
                PostId.objectify(postId),
                parentId,
                viewerId,
                sort,
                page,
                size
        );

        log.debug("Listing comments for post={}, parentId={}, viewer={}, sort={}, page={}, size={}",
                postId,
                parentIdRaw,
                viewerId != null ? viewerId.stringify() : "ANONYMOUS",
                sort,
                page,
                size
        );

        return listCommentUseCase.listComment(query);
    }
}
