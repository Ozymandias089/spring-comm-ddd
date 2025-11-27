package com.y11i.springcommddd.comments.application.port.in;

import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;

/**
 * 댓글 생성 유스케이스.
 *
 * <p>
 * - 루트 댓글과 대댓글(reply)을 모두 처리한다.<br>
 * - 부모 댓글 ID가 null이면 루트 댓글로, 아니면 대댓글로 간주한다.
 * </p>
 */
public interface CreateCommentUseCase {

    /**
     * 새 댓글을 생성한다.
     *
     * @param cmd 작성 정보
     * @return 생성된 댓글 ID
     */
    CommentId create(CreateCommentCommand cmd);

    /**
     * 댓글 생성 커맨드.
     *
     * @param authorId  작성자 ID
     * @param postId    게시글 ID
     * @param body      댓글 본문
     * @param parentId  부모 댓글 ID (루트 댓글이면 null)
     */
    record CreateCommentCommand(
            MemberId authorId,
            PostId postId,
            String body,
            CommentId parentId   // Root 댓글의 경우 NULL
    ){}
}
