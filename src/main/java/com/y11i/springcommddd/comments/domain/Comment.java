package com.y11i.springcommddd.comments.domain;

import com.y11i.springcommddd.comments.domain.exception.CommentDeletedModificationNotAllowed;
import com.y11i.springcommddd.comments.domain.exception.InvalidCommentDepth;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

/**
 * 댓글(Comment) 애그리게잇 루트.
 *
 * <p><b>개요</b><br>
 * 게시글({@link PostId})에 속한 댓글을 표현하는 도메인 모델입니다.
 * 루트 댓글과 대댓글(부모가 있는 댓글) 모두를 표현하며,
 * 본문, 작성자, 상태(표시/삭제), 투표 집계(추천/비추천) 정보를 포함합니다.
 * </p>
 *
 * <p><b>영속성/테이블</b></p>
 * <ul>
 *   <li>테이블: {@code comments}</li>
 *   <li>인덱스:
 *     <ul>
 *       <li>{@code post_id}</li>
 *       <li>{@code parent_id}</li>
 *       <li>{@code (post_id, parent_id, created_at)} — 루트 및 자식 댓글의 시간순 조회 최적화</li>
 *     </ul>
 *   </li>
 *   <li>감사 필드: {@link #createdAt}, {@link #updatedAt}</li>
 *   <li>낙관적 락 버전: {@link #version}</li>
 * </ul>
 *
 * <p><b>불변식/규칙</b></p>
 * <ul>
 *   <li>{@code depth &gt;= 0}</li>
 *   <li>삭제 상태({@link CommentStatus#DELETED})인 댓글은 편집/재삭제 불가</li>
 *   <li>투표 집계는 0 미만으로 내려가지 않음</li>
 * </ul>
 */
@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_post", columnList = "post_id"),
        @Index(name = "idx_comments_parent", columnList = "parent_id"),
        @Index(name = "idx_comments_post_parent_created", columnList = "post_id, parent_id, created_at")
})
@EntityListeners(AuditingEntityListener.class)
@Access(AccessType.FIELD)
public class Comment {

    @EmbeddedId
    private CommentId commentId;

    @Embedded
    @AttributeOverride( name = "id", column = @Column(name = "post_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false) )
    private PostId postId;

    @Embedded
    @AttributeOverride( name = "id", column = @Column(name = "author_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false) )
    private MemberId authorId;

    @Embedded
    @AttributeOverride( name = "id", column = @Column(name = "parent_id", columnDefinition = "BINARY(16)") )
    private CommentId parentId;

    @Column(name = "depth", nullable = false)
    private int depth;

    @Embedded
    private CommentBody body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CommentStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    @Column(name="up_count", nullable=false)
    private int upCount = 0;

    @Column(name="down_count", nullable=false)
    private int downCount = 0;

    /** JPA 기본 생성자. 외부에서 직접 호출하지 않습니다. */
    protected  Comment() {}

    private Comment(PostId postId, MemberId authorId, CommentId parentId, int depth, CommentBody body) {
        this.commentId = CommentId.newId();
        this.postId = Objects.requireNonNull(postId);
        this.authorId = Objects.requireNonNull(authorId);
        this.parentId = parentId; // nullable
        if (depth < 0) throw new InvalidCommentDepth(depth);
        this.depth = depth;
        this.body = Objects.requireNonNull(body);
        this.status = CommentStatus.VISIBLE;
    }

    // -----------------------------------------------------
    // 정적 팩토리 (생성 섹션)
    // -----------------------------------------------------

    /**
     * 루트 댓글을 생성합니다.
     *
     * @param postId   댓글이 속한 게시글 식별자
     * @param authorId 작성자 식별자
     * @param body     본문(문자열). 공백/빈값 금지
     * @return 생성된 루트 댓글
     * @throws CommentDeletedModificationNotAllowed 본문이 비어 있거나 null인 경우
     */
    public static Comment createRoot(PostId postId, MemberId authorId, String body) {
        return new Comment(postId, authorId, null, 0, new CommentBody(body));
    }

    public static Comment replyTo(PostId postId, MemberId authorId, Comment parent, String body) {
        Objects.requireNonNull(parent, "parent comment required");
        if (!parent.postId().equals(postId)) {
            throw new IllegalArgumentException("parent belongs to different post");
        }
        return new Comment(
                postId,
                authorId,
                parent.commentId(),
                parent.depth() + 1,
                new CommentBody(body)
        );
    }

    // -----------------------------------------------------
    // 도메인 동작 (수정/삭제/집계 섹션)
    // -----------------------------------------------------

    /**
     * 댓글 본문을 수정합니다.
     *
     * <p><b>전제조건</b>: 삭제된 댓글이 아니어야 합니다.</p>
     * <p><b>부작용</b>: {@link #body}가 새로운 값으로 대체됩니다.</p>
     *
     * @param newBody 새 본문(문자열). 공백/빈값 금지
     * @throws CommentDeletedModificationNotAllowed 삭제된 댓글을 수정하려고 한 경우
     * @throws IllegalArgumentException 본문이 비어 있거나 null인 경우
     */
    public void edit(String newBody) {
        ensureNotDeleted("Deleted comment cannot be edited");
        this.body = new CommentBody(newBody);
    }

    /**
     * 댓글을 소프트 삭제 상태로 전환합니다.
     *
     * <p><b>전제조건</b>: 이미 삭제되지 않았어야 합니다.</p>
     * <p><b>부작용</b>: 상태가 {@link CommentStatus#DELETED}로 변경됩니다.</p>
     *
     * @throws IllegalStateException 이미 삭제된 댓글을 다시 삭제하려는 경우
     */
    public void softDelete() {
        ensureNotDeleted("Deleted comment cannot be soft-deleted");
        this.status = CommentStatus.DELETED;
    }

    /**
     * 투표 변경에 따른 집계(up/down 카운트)를 업데이트합니다.
     *
     * <p><b>의도</b>: 이전 투표값과 새로운 투표값의 차이를 반영하여 카운터를 정확히 갱신합니다.</p>
     * <p><b>규칙</b>:
     *   <ul>
     *     <li>{@code oldValue/newValue} ∈ {-1, 0, 1}</li>
     *     <li>동일 값인 경우 변경 없음</li>
     *     <li>결과 카운터는 0 미만으로 내려가지 않음</li>
     *   </ul>
     * </p>
     *
     * @param oldValue 이전 투표값 (-1: 비추천, 0: 없음, 1: 추천)
     * @param newValue 새로운 투표값 (-1: 비추천, 0: 없음, 1: 추천)
     */
    public void applyVoteDelta(int oldValue, int newValue){
        if (oldValue == newValue) return;
        if (oldValue == 1) upCount--;
        if (oldValue == -1) downCount--;
        if (newValue == 1) upCount++;
        if (newValue == -1) downCount++;
        if (upCount < 0) upCount = 0;
        if (downCount < 0) downCount = 0;
    }

    // -----------------------------------------------------
    // 내부 검증 유틸
    // -----------------------------------------------------

    /**
     * 삭제 상태가 아님을 보장합니다.
     *
     * @throws CommentDeletedModificationNotAllowed 삭제 상태인 경우
     */
    private void ensureNotDeleted(String msg) {
        if (status == CommentStatus.DELETED) throw new CommentDeletedModificationNotAllowed(msg);
    }

    // -----------------------------------------------------
    // 접근자 (게터 섹션)
    // -----------------------------------------------------

    /** 댓글 식별자 */
    public CommentId commentId() { return commentId; }
    /** 소속 게시글 식별자 */
    public PostId postId() { return postId; }
    /** 작성자 식별자 */
    public MemberId authorId() { return authorId; }
    /** 부모 댓글 식별자 (루트 댓글인 경우 {@code null}) */
    public CommentId parentId() { return parentId; }
    /** 댓글 깊이(루트=0) */
    public int depth() { return depth; }
    /** 댓글 본문 값 객체 */
    public CommentBody body() { return body; }
    /** 댓글 상태 */
    public CommentStatus status() { return status; }
    /** 생성 시각(감사) */
    public Instant createdAt() { return createdAt; }
    /** 수정 시각(감사) */
    public Instant updatedAt() { return updatedAt; }
    /** 낙관적 락 버전 */
    public long version() { return version; }
    /** 추천 수를 반환합니다. */
    public int upCount(){ return upCount; }
    /** 비추천 수를 반환합니다. */
    public int downCount(){ return downCount; }
    /** 점수(추천 - 비추천)를 계산해 반환합니다. */
    public int score(){ return upCount - downCount; }
}
