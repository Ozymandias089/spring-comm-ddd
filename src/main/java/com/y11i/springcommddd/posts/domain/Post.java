package com.y11i.springcommddd.posts.domain;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "posts")
@EntityListeners(AuditingEntityListener.class)
@Access(AccessType.FIELD)
public class Post implements AggregateRoot {

    @EmbeddedId
    private PostId postId;

    @Embedded
    @AttributeOverride(
            name = "id",
            column = @Column(name = "community_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    )
    private CommunityId communityId;

    @Embedded
    @AttributeOverride(
            name = "id",
            column = @Column(name = "author_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    )
    private MemberId authorId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "title", nullable = false, length = 200))
    private Title title;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "content", nullable = false, columnDefinition = "LONGTEXT"))
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    private Content content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PostStatus status;

    @Column(name = "published_at")
    private Instant publishedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    /** JPA 전용 */
    protected Post() {}

    /** 도메인에서 새 글 만들 때: postId 내부에서 생성 */
    private Post(CommunityId communityId, MemberId authorId, Title title, Content content) {
        this.postId = PostId.newId();
        this.authorId = Objects.requireNonNull(authorId);
        rename(title);
        rewrite(content);
        this.status = PostStatus.DRAFT;
    }

    /** 퍼블릭 팩토리: 도메인에서 사용 */
    public static Post create(CommunityId communityId, MemberId authorId, String title, String content) {
        return new Post(communityId, authorId, new Title(title), new Content(content));
    }

    // --- 도메인 메서드 ---

    /** Rename Post - Forbidden on ARCHIVED Status */
    public void rename(String newTitle) {rename(new Title(newTitle));}
    public void rename(Title newTitle) {
        ensureNotArchived("Cannot rename an Archived Post");
        this.title = newTitle;
    }

    /** Edit contents - Allowed on PUBLISHED status */
    public void rewrite(String newContent) {rewrite(new Content(newContent));}
    public void rewrite(Content newContent) {
        ensureNotArchived("Cannot edit contents of an archived post");
        this.content = newContent;
    }

    /** Publish - only on DRAFT status */
    public void publish(){
        if (status != PostStatus.DRAFT) throw new IllegalStateException("Only DRAFT status can be published");
        this.status = PostStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    /** Archive - if already archived: no Action */
    public void archive(){
        if (status == PostStatus.ARCHIVED) return ;
        this.status = PostStatus.ARCHIVED;
    }

    /** DEBUG - Restore Archived Posts */
    public void restore() {
        if (status != PostStatus.ARCHIVED) throw new  IllegalStateException("Only ARCHIVED status can be restored");
        this.status = PostStatus.PUBLISHED;
    }

    /** Check if PostStatus is Archived */
    public void ensureNotArchived(String message) {
        if (status == PostStatus.ARCHIVED) throw new IllegalStateException(message);
    }

    /** Accessors (Read-only) */
    public PostId postId() { return postId; }
    public CommunityId communityId() { return communityId; }
    public MemberId authorId() { return authorId; }
    public Title title() { return title; }
    public Content content() { return content; }
    public PostStatus status() { return status; }
    public Instant publishedAt() { return publishedAt; }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
    public long version() { return version; }

}