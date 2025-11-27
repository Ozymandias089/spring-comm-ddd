package com.y11i.springcommddd.comments.api;

import com.y11i.springcommddd.comments.application.port.in.DeleteCommentUseCase;
import com.y11i.springcommddd.comments.application.port.in.EditCommentUseCase;
import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.comments.dto.request.EditCommentRequestDTO;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 댓글 수정/삭제 API 컨트롤러.
 *
 * <p>
 * - PATCH /api/comments/{commentId} : 댓글 본문 수정<br>
 * - DELETE /api/comments/{commentId} : 댓글 소프트 삭제
 * </p>
 *
 * <p>
 * 권한/밴 정책은 애플리케이션 서비스에서 처리한다.
 * </p>
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentMutationController {
    private final EditCommentUseCase editCommentUseCase;
    private final DeleteCommentUseCase deleteCommentUseCase;

    // ───────────────────────── 댓글 수정 ─────────────────────────

    /**
     * 댓글 본문을 수정한다.
     *
     * <p>
     * - 인증된 사용자만 호출 가능 (@AuthenticatedMember)<br>
     * - commentId는 URL path에서, body는 요청 JSON으로 전달<br>
     * - 작성자 본인이면서, 해당 커뮤니티에서 밴 당하지 않은 경우에만 허용
     * </p>
     *
     * 예:
     * <pre>
     * PATCH /api/comments/{commentId}
     * {
     *   "body": "수정된 댓글 내용"
     * }
     * </pre>
     */
    @PatchMapping(path = "/{commentId}", consumes = "application/json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editComment(
            @AuthenticatedMember MemberId actorId,
            @PathVariable("commentId") String commentIdRaw,
            @Valid @RequestBody EditCommentRequestDTO requestDTO
    ) {
        var commentId = CommentId.objectify(commentIdRaw);

        editCommentUseCase.edit(
                new EditCommentUseCase.EditCommentCommand(
                        commentId,
                        actorId,
                        requestDTO.body()
                )
        );

        log.info("Edited comment {} by actor {}", commentId.stringify(), actorId.stringify());
    }

    // ───────────────────────── 댓글 삭제 ─────────────────────────

    /**
     * 댓글을 소프트 삭제한다.
     *
     * <p>
     * - 인증된 사용자만 호출 가능<br>
     * - 댓글 작성자 본인 또는 해당 커뮤니티 ADMIN/MOD만 삭제 가능<br>
     * - 밴 여부는 상관 없음
     * </p>
     *
     * 예:
     * <pre>
     * DELETE /api/comments/{commentId}
     * </pre>
     */
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @AuthenticatedMember MemberId actorId,
            @PathVariable("commentId") String commentIdRaw
    ) {
        var commentId = CommentId.objectify(commentIdRaw);

        deleteCommentUseCase.delete(
                new DeleteCommentUseCase.DeleteCommentCommand(
                        commentId,
                        actorId
                )
        );

        log.info("Soft-deleted comment {} by actor {}", commentId.stringify(), actorId.stringify());
    }
}
