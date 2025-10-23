package com.y11i.springcommddd.comments.domain;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

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

    protected  Comment() {}

    private Comment(PostId postId, MemberId authorId, CommentId parentId, int depth, CommentBody body) {
        this.commentId = CommentId.newId();
        this.postId = Objects.requireNonNull(postId);
        this.authorId = Objects.requireNonNull(authorId);
        this.parentId = parentId; // nullable
        if (depth < 0) throw new IllegalArgumentException("depth must be >= 0");
        this.depth = depth;
        this.body = Objects.requireNonNull(body);
        this.status = CommentStatus.VISIBLE;
    }

    /** Root Comment */
    public static Comment createRoot(PostId postId, MemberId authorId, String body) {
        return new Comment(postId, authorId, null, 0, new CommentBody(body));
    }

    /** Create replies */
    public static Comment replyTo(PostId postId, MemberId authorId, CommentId parentId, int parentDepth, String body) {
        return new Comment(postId, authorId, parentId, parentDepth+1,  new CommentBody(body));
    }

    // --- 도메인 동작 ---

    public void edit(String newBody) {
        ensureNotDeleted("Deleted comment cannot be edited");
        this.body = new CommentBody(newBody);
    }

    public void softDelete() {
        ensureNotDeleted("Deleted comment cannot be soft-deleted");
        this.status = CommentStatus.DELETED;
    }

    private void ensureNotDeleted(String msg) {
        if (status == CommentStatus.DELETED) throw new IllegalStateException(msg);
    }

    // --- 접근자 ---
    public CommentId commentId() { return commentId; }
    public PostId postId() { return postId; }
    public MemberId authorId() { return authorId; }
    public CommentId parentId() { return parentId; }
    public int depth() { return depth; }
    public CommentBody body() { return body; }
    public CommentStatus status() { return status; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public long version() { return version; }
}
