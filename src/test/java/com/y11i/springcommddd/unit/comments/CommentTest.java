package com.y11i.springcommddd.unit.comments;

import com.y11i.springcommddd.comments.domain.*;
import com.y11i.springcommddd.comments.domain.exception.CommentDeletedModificationNotAllowed;
import com.y11i.springcommddd.comments.domain.exception.InvalidCommentDepth;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class CommentTest {

    // --- Happy: 루트 댓글 생성 ---
    @Test
    @DisplayName("createRoot: depth=0, parentId=null, status=VISIBLE")
    void createRoot_ok() {
        var postId = PostId.newId();
        var authorId = MemberId.newId();

        var c = Comment.createRoot(postId, authorId, "root body");

        assertSoftly(s -> {
            s.assertThat(c.postId()).isEqualTo(postId);
            s.assertThat(c.authorId()).isEqualTo(authorId);
            s.assertThat(c.parentId()).isNull();
            s.assertThat(c.depth()).isEqualTo(0);
            s.assertThat(c.status()).isEqualTo(CommentStatus.VISIBLE);
            s.assertThat(c.body().value()).isEqualTo("root body");
        });
    }

//    // --- Happy: 대댓글 생성 ---
//    @Test
//    @DisplayName("replyTo: depth는 parentDepth+1, parentId 설정")
//    void replyTo_ok() {
//        var postId = PostId.newId();
//        var authorId = MemberId.newId();
//        var parentId = CommentId.newId();
//
//        var child = Comment.replyTo(postId, authorId, parentId, /*parentDepth=*/1, "child body");
//
//        assertSoftly(s -> {
//            s.assertThat(child.parentId()).isEqualTo(parentId);
//            s.assertThat(child.depth()).isEqualTo(2);
//            s.assertThat(child.body().value()).isEqualTo("child body");
//            s.assertThat(child.status()).isEqualTo(CommentStatus.VISIBLE);
//        });
//    }
//
//    // --- Guard: 음수 depth 생성 방지 ---
//    @Test
//    @DisplayName("InvalidCommentDepth: depth<0이면 예외")
//    void invalid_depth_throws() {
//        var postId = PostId.newId();
//        var authorId = MemberId.newId();
//        assertThatThrownBy(() ->
//                // parentDepth = -2 → depth = -1 → 예외 발생
//                Comment.replyTo(postId, authorId, CommentId.newId(), -2, "x")
//        ).isInstanceOf(InvalidCommentDepth.class);
//    }
//
//    @Test
//    @DisplayName("replyTo: parentDepth=-1 → depth=0 (예외 없음)")
//    void replyTo_parentDepth_minus1_becomes_zero() {
//        var c = Comment.replyTo(PostId.newId(), MemberId.newId(), CommentId.newId(), -1, "ok");
//        assertThat(c.depth()).isEqualTo(0);
//    }
//
    // --- 수정: 삭제 상태가 아니면 edit 가능 ---
    @Test
    @DisplayName("edit: 삭제 상태가 아니면 본문 수정 가능")
    void edit_when_visible_ok() {
        var c = Comment.createRoot(PostId.newId(), MemberId.newId(), "old");
        c.edit("new");
        assertThat(c.body().value()).isEqualTo("new");
    }

    // --- 삭제: softDelete는 상태를 DELETED로 ---
    @Test
    @DisplayName("softDelete: status=DELETED로 전환")
    void softDelete_ok() {
        var c = Comment.createRoot(PostId.newId(), MemberId.newId(), "body");
        c.softDelete();
        assertThat(c.status()).isEqualTo(CommentStatus.DELETED);
    }

    // --- Guard: 삭제 상태에서는 edit/재삭제 모두 금지 ---
    @Test
    @DisplayName("DELETED 이후 edit/softDelete 재호출 → CommentDeletedModificationNotAllowed")
    void edit_or_softDelete_again_when_deleted_forbidden() {
        var c = Comment.createRoot(PostId.newId(), MemberId.newId(), "body");
        c.softDelete();

        assertThatThrownBy(() -> c.edit("x"))
                .isInstanceOf(CommentDeletedModificationNotAllowed.class);

        // 이미 DELETED 상태에서 softDelete() 재호출도 내부 ensureNotDeleted에 걸림
        assertThatThrownBy(c::softDelete)
                .isInstanceOf(CommentDeletedModificationNotAllowed.class);
    }

    // --- 투표 집계: 델타 반영 및 0 미만 방지 ---
    @Test
    @DisplayName("applyVoteDelta: 1→-1, -1→0, 0→1 등 델타 반영; 카운터 0 미만 방지")
    void applyVoteDelta_updates_counters_and_never_negative() {
        var c = Comment.createRoot(PostId.newId(), MemberId.newId(), "body");

        // 0 -> 1 : upCount +1
        c.applyVoteDelta(0, 1);
        assertThat(c.upCount()).isEqualTo(1);
        assertThat(c.downCount()).isEqualTo(0);

        // 1 -> -1 : upCount -1, downCount +1
        c.applyVoteDelta(1, -1);
        assertThat(c.upCount()).isEqualTo(0);
        assertThat(c.downCount()).isEqualTo(1);

        // -1 -> 0 : downCount -1
        c.applyVoteDelta(-1, 0);
        assertThat(c.upCount()).isEqualTo(0);
        assertThat(c.downCount()).isEqualTo(0);

        // 방어 로직 확인: (이전 값 1이었으나 실제 카운트는 0인 엣지) → 결과 음수 방지
        c.applyVoteDelta(1, 0); // 내부적으로 -1 후 clamp to 0
        assertThat(c.upCount()).isEqualTo(0);
        assertThat(c.downCount()).isEqualTo(0);
    }
}
