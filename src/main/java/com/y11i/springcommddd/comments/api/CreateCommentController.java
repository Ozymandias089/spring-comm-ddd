package com.y11i.springcommddd.comments.api;

import com.y11i.springcommddd.comments.application.port.in.CreateCommentUseCase;
import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.comments.dto.request.CreateCommentRequestDTO;
import com.y11i.springcommddd.comments.dto.response.CreateCommentResponseDTO;
import com.y11i.springcommddd.iam.api.support.AuthenticatedMember;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class CreateCommentController {
    private final CreateCommentUseCase createCommentUseCase;

    /**
     * 댓글 생성 (루트 + 대댓글 공통).
     *
     * <p>
     * - POST /api/posts/{postId}/comments<br>
     * - parentId 파라미터가 없으면 루트 댓글<br>
     * - parentId 파라미터가 있으면 해당 댓글의 대댓글로 간주
     * </p>
     */
    @PostMapping(path = "/{postId}/comments", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateCommentResponseDTO createComment(
            @AuthenticatedMember MemberId authorId,
            @PathVariable(name = "postId") String postId,
            @RequestParam(value = "parentId", required = false) String parentIdRaw,
            @Valid @RequestBody CreateCommentRequestDTO requestDTO
    ){
        CommentId parentId = (parentIdRaw != null)
                ? CommentId.objectify(parentIdRaw)
                : null;
        log.debug("Parent comment id: {}", parentId);

        CommentId commentId = createCommentUseCase.create(
                new CreateCommentUseCase.CreateCommentCommand(
                        authorId,
                        PostId.objectify(postId),
                        requestDTO.body(),
                        parentId
                )
        );
        log.debug("Comment id: {}", commentId.stringify());
        return CreateCommentResponseDTO.builder().commentId(commentId.stringify()).build();
    }
}
