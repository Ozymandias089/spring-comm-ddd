package com.y11i.springcommddd.comments.application.service;

import com.y11i.springcommddd.comments.application.port.in.DeleteCommentUseCase;
import com.y11i.springcommddd.comments.application.port.in.EditCommentUseCase;
import com.y11i.springcommddd.comments.application.port.out.LoadCommentPort;
import com.y11i.springcommddd.comments.application.port.out.LoadPostForCommentPort;
import com.y11i.springcommddd.comments.application.port.out.SaveCommentPort;
import com.y11i.springcommddd.comments.domain.Comment;
import com.y11i.springcommddd.comments.domain.CommentStatus;
import com.y11i.springcommddd.comments.domain.exception.CommentNotFound;
import com.y11i.springcommddd.comments.domain.exception.CommentStatusTransitionNotAllowed;
import com.y11i.springcommddd.communities.application.port.internal.CommunityAuthorization;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.application.port.out.CheckCommunityBanPort;
import com.y11i.springcommddd.posts.application.port.out.SavePostPort;
import com.y11i.springcommddd.posts.domain.Post;
import com.y11i.springcommddd.posts.domain.exception.PostNotFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentMutationService implements EditCommentUseCase, DeleteCommentUseCase {
    private final LoadCommentPort loadCommentPort;
    private final SaveCommentPort saveCommentPort;
    private final LoadPostForCommentPort loadPostForCommentPort;
    private final SavePostPort savePostPort;
    private final CheckCommunityBanPort checkCommunityBanPort;
    private final CommunityAuthorization communityAuthorization;

    @Override
    @Transactional
    public void edit(EditCommentCommand cmd) {
        Comment comment = loadCommentPort.loadById(cmd.commentId()).orElseThrow(() -> new CommentNotFound("Comment not found"));

        // 댓글이 속한 게시글 + 커뮤니티 로드
        Post post = loadPostForCommentPort.loadById(comment.postId())
                .orElseThrow(() -> new PostNotFound("Post not found: " + comment.postId().stringify()));

        // 1) 작성자 본인인지 확인
        ensureOwner(comment, cmd.actorId());

        // 2) 해당 커뮤니티에서 밴 당한 상태가 아닌지 확인
        checkCommunityBanPort.ensureNotBanned(post.communityId(), cmd.actorId());

        // 3) 도메인 로직: 본문 수정 + isEdited 플래그 true
        comment.edit(cmd.body());

        saveCommentPort.save(comment);

        log.info("Edited comment {} on post {} by actor {}",
                comment.commentId().stringify(),
                comment.postId().stringify(),
                cmd.actorId().stringify());
    }

    @Override
    @Transactional
    public void delete(DeleteCommentCommand cmd) {
        Comment comment = loadCommentPort.loadById(cmd.commentId())
                .orElseThrow(() -> new CommentNotFound("Comment not found"));

        // 댓글이 속한 게시글 + 커뮤니티 로드
        Post post = loadPostForCommentPort.loadById(comment.postId())
                .orElseThrow(() -> new PostNotFound("Post not found: " + comment.postId().stringify()));

        // 1) 권한 체크
        ensureDeletePermission(comment, post, cmd.actorId());

        // 2) 삭제 전 상태 기억 (보이던 댓글만 카운트 감소 대상)
        boolean wasVisible = (comment.status() == CommentStatus.VISIBLE);

        // 3) 도메인 로직: 소프트 삭제
        comment.softDelete();
        saveCommentPort.save(comment);

        // 4) Post의 commentCount 조정 (VISIBLE → DELETED 인 경우만 감소)
        if (wasVisible) {
            post.applyCommentVisibilityChange(true, false);
            savePostPort.save(post);
        }

        log.info("Soft-deleted comment {} on post {} by actor {}",
                comment.commentId().stringify(),
                comment.postId().stringify(),
                cmd.actorId().stringify());
    }

    // ───────────────────────── 내부 공통 유틸 ─────────────────────────

    /**
     * 작성자(owner) 검증: actorId가 댓글 작성자와 동일한지 확인.
     * 추후 ADMIN/MOD 권한 삭제 허용 시 여기에서 분기 추가 가능.
     */
    private void ensureOwner(Comment comment, MemberId actorId) {
        if (!comment.authorId().equals(actorId)) {
            throw new CommentStatusTransitionNotAllowed("You can modify only your own comments");
        }
    }

    /**
     * 삭제 권한 체크:
     * - 작성자 본인이면 허용
     * - 아니면 해당 커뮤니티의 ADMIN 또는 MOD 여야 함
     */
    private void ensureDeletePermission(Comment comment, Post post, MemberId actorId) {
        // 작성자 본인이면 통과
        if (comment.authorId().equals(actorId)) {
            return;
        }

        // 아니면 커뮤니티 관리자/모더레이터 권한 요구
        try {
            communityAuthorization.requireAdminOrModerator(actorId, post.communityId());
        } catch (AccessDeniedException ex) {
            // requireAdminOrModerator가 AccessDeniedException 던진다면 그대로 전파
            throw ex;
        } catch (RuntimeException ex) {
            // 구현이 다른 예외를 던질 수도 있으니 래핑
            throw new CommentStatusTransitionNotAllowed("You are not allowed to delete this comment : "+ex.getMessage());
        }
    }
}
