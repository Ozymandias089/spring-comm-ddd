package com.y11i.springcommddd.posts.domain;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.exception.ArchivedPostModificationNotAllowed;
import com.y11i.springcommddd.posts.domain.exception.PostStatusTransitionNotAllowed;
import com.y11i.springcommddd.shared.domain.AggregateRoot;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

/**
 * 게시글(Post) 애그리게잇 루트.
 *
 * <p><b>개요</b><br>
 * 커뮤니티({@link CommunityId})에 속한 게시글로, 작성자({@link MemberId}),
 * 제목/본문 값 객체({@link Title}, {@link Content}), 상태({@link PostStatus}),
 * 공개 시각, 감사 필드, 투표 집계(추천/비추천)를 관리합니다.
 * </p>
 *
 * <p><b>영속성/테이블</b></p>
 * <ul>
 *   <li>테이블: {@code posts}</li>
 *   <li>본문 컬럼: {@code LONGTEXT} (Hibernate {@link SqlTypes#LONGVARCHAR} 매핑)</li>
 *   <li>감사 필드: {@link #createdAt}, {@link #updatedAt}</li>
 *   <li>낙관적 락: {@link #version}</li>
 * </ul>
 *
 * <p><b>불변식/규칙</b></p>
 * <ul>
 *   <li>{@link Title}, {@link Content}는 null/공백 등 자체 검증을 통과해야 함</li>
 *   <li>보관({@link PostStatus#ARCHIVED}) 상태에서는 편집/제목변경 금지</li>
 *   <li>게시(Publish)는 초안({@link PostStatus#DRAFT})에서만 가능</li>
 *   <li>투표 집계는 0 미만으로 내려가지 않음</li>
 * </ul>
 */
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

    @Column(name="up_count", nullable=false)
    private int upCount = 0;

    @Column(name="down_count", nullable=false)
    private int downCount = 0;

    /** JPA 전용 */
    protected Post() {}

    /**
     * 내부 생성자: 새 글을 생성합니다.
     * <p><b>부작용</b>:
     * <ul>
     *   <li>{@link #postId}는 신규 발급</li>
     *   <li>{@link #status}는 {@link PostStatus#DRAFT}로 초기화</li>
     *   <li>제목/본문은 검증 후 설정</li>
     * </ul>
     * </p>
     */
    private Post(CommunityId communityId, MemberId authorId, Title title, Content content) {
        this.postId = PostId.newId();
        this.communityId = Objects.requireNonNull(communityId);
        this.authorId = Objects.requireNonNull(authorId);
        rename(title);
        rewrite(content);
        this.status = PostStatus.DRAFT;
    }

    // -----------------------------------------------------
    // 생성 섹션 (정적 팩토리)
    // -----------------------------------------------------

    /**
     * 새 게시글을 생성합니다.
     *
     * @param communityId 커뮤니티 식별자
     * @param authorId 작성자 식별자
     * @param title 제목 문자열(검증됨)
     * @param content 본문 문자열(검증됨)
     * @return 새 {@link Post}
     * @throws IllegalArgumentException 제목/본문 규칙 위반
     */
    public static Post create(CommunityId communityId, MemberId authorId, String title, String content) {
        return new Post(communityId, authorId, new Title(title), new Content(content));
    }

    // -----------------------------------------------------
    // 도메인 동작 섹션 (제목/본문/상태 전이/투표 집계)
    // -----------------------------------------------------

    /**
     * 제목을 변경합니다. (보관 상태에서 금지)
     *
     * @param newTitle 새 제목 문자열
     * @throws IllegalStateException 보관 상태에서 변경 시도
     * @throws IllegalArgumentException 제목 규칙 위반
     */
    public void rename(String newTitle) {rename(new Title(newTitle));}

    /**
     * 제목을 변경합니다. (보관 상태에서 금지)
     */
    public void rename(Title newTitle) {
        ensureNotArchived("Cannot rename an Archived Post");
        this.title = newTitle;
    }

    /**
     * 본문을 수정합니다. (게시됨 상태도 허용, 단 보관 상태는 금지)
     *
     * @param newContent 새 본문 문자열
     * @throws IllegalStateException 보관 상태에서 변경 시도
     * @throws IllegalArgumentException 본문 규칙 위반
     */
    public void rewrite(String newContent) {rewrite(new Content(newContent));}

    /**
     * 본문을 수정합니다. (보관 상태 금지)
     */
    public void rewrite(Content newContent) {
        ensureNotArchived("Cannot edit contents of an archived post");
        this.content = newContent;
    }

    /**
     * 게시(Publish)합니다. (초안 상태에서만 가능)
     *
     * <p><b>부작용</b>: 상태가 {@link PostStatus#PUBLISHED}로 전환되고,
     * {@link #publishedAt}가 현재 시각으로 설정됩니다.</p>
     *
     * @throws PostStatusTransitionNotAllowed DRAFT가 아닌 상태에서 호출한 경우
     */
    public void publish(){
        if (status != PostStatus.DRAFT) throw new PostStatusTransitionNotAllowed("Only DRAFT status can be published");
        this.status = PostStatus.PUBLISHED;
        this.publishedAt = Instant.now();
    }

    /**
     * 보관(Archive)합니다. (이미 보관이면 무시)
     */
    public void archive(){
        if (status == PostStatus.ARCHIVED) return ;
        this.status = PostStatus.ARCHIVED;
    }

    /**
     * 보관 상태의 게시글을 복구합니다. (디버그/운영 정책에 따라 제한 가능)
     *
     * @throws PostStatusTransitionNotAllowed 보관 상태가 아닌 경우
     */
    public void restore() {
        if (status != PostStatus.ARCHIVED) throw new PostStatusTransitionNotAllowed("Only ARCHIVED status can be restored");
        this.status = PostStatus.PUBLISHED;
    }

    /**
     * 투표 변경에 따른 집계값을 갱신합니다.
     *
     * <p><b>의도</b>: 이전 값과 새 값을 비교해 up/down 카운트를 정확히 보정합니다.</p>
     * <p><b>규칙</b>: 값 ∈ {-1, 0, 1}, 결과 카운트는 0 미만 불가.</p>
     *
     * @param oldValue 이전 투표값 (-1/0/1)
     * @param newValue 새로운 투표값 (-1/0/1)
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
    // 내부 검증 섹션
    // -----------------------------------------------------

    /**
     * 게시글이 보관 상태가 아님을 보장합니다.
     *
     * @param message 예외 메시지
     * @throws ArchivedPostModificationNotAllowed 보관 상태인 경우
     */
    public void ensureNotArchived(String message) {
        if (status == PostStatus.ARCHIVED) throw new ArchivedPostModificationNotAllowed(message);
    }

    // -----------------------------------------------------
    // 접근자 섹션 (읽기 전용)
    // -----------------------------------------------------

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
    public int upCount(){ return upCount; }
    public int downCount(){ return downCount; }
    public int score(){ return upCount - downCount; }
}