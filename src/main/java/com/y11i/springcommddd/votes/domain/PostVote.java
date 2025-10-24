package com.y11i.springcommddd.votes.domain;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.shared.domain.AggregateRoot;
import jakarta.persistence.*;

import java.util.Objects;

/**
 * 게시글 투표(PostVote) 애그리게잇 루트.
 *
 * <p><b>개요</b><br>
 * 특정 게시글({@link PostId})에 대해 특정 회원({@link MemberId})이 남긴
 * 단일 투표(값: +1 또는 -1)를 표현합니다.
 * </p>
 *
 * <p><b>영속성/제약</b></p>
 * <ul>
 *   <li>테이블: {@code post_votes}</li>
 *   <li>유니크 제약: {@code uk_post_votes_target_voter} (post_id, voter_id)</li>
 *   <li>PK: {@link PostVoteId} (UUID)</li>
 * </ul>
 *
 * <p><b>규칙</b></p>
 * <ul>
 *   <li>투표값은 +1 또는 -1만 허용</li>
 *   <li>동일 회원은 동일 게시글에 하나의 투표만 보유</li>
 * </ul>
 */
@Entity
@Table(name = "post_votes",
        uniqueConstraints = @UniqueConstraint(name = "uk_post_votes_target_voter", columnNames = {"post_id","voter_id"}))
@Access(AccessType.FIELD)
public class PostVote implements AggregateRoot {

    @EmbeddedId
    private PostVoteId postVoteId;

    @Embedded
    @AttributeOverride(name="id", column=@Column(name="post_id", columnDefinition="BINARY(16)", nullable=false, updatable=false))
    private PostId postId;

    @Embedded
    @AttributeOverride(name="id", column=@Column(name="voter_id", columnDefinition="BINARY(16)", nullable=false, updatable=false))
    private MemberId voterId;

    /** 투표 값: +1(추천), -1(비추천) */
    @Column(name = "value", nullable = false)
    private int value;

    protected PostVote() {}

    /**
     * 내부 생성자: 투표를 생성합니다.
     *
     * @param postId  게시글 식별자
     * @param voterId 투표자 식별자
     * @param value   투표값 (+1 또는 -1)
     * @throws NullPointerException postId/voterId가 null인 경우
     * @throws IllegalArgumentException value가 +1/-1 이외인 경우
     */
    public PostVote(PostId postId, MemberId voterId, int value) {
        this.postVoteId = PostVoteId.newId();
        this.postId = Objects.requireNonNull(postId);
        this.voterId = Objects.requireNonNull(voterId);
        setValue(value);
    }

    // -----------------------------------------------------
    // 생성 섹션 (정적 팩토리)
    // -----------------------------------------------------

    /**
     * 투표를 생성합니다.
     *
     * @param postId  게시글 식별자
     * @param voterId 투표자 식별자
     * @param value   투표값 (+1 또는 -1)
     */
    public static PostVote cast(PostId postId, MemberId voterId, int value) {
        return new PostVote(postId, voterId, value);
    }

    // -----------------------------------------------------
    // 도메인 동작 섹션
    // -----------------------------------------------------

    /**
     * 투표값을 설정합니다.
     *
     * @param v +1(추천) 또는 -1(비추천)
     * @throws IllegalArgumentException 허용되지 않은 값
     */
    public void setValue(int v) {
        if (v != 1 && v != -1) throw new IllegalArgumentException("vote must be 1 or -1");
        this.value = v;
    }

    // -----------------------------------------------------
    // 접근자 섹션
    // -----------------------------------------------------

    public PostVoteId postVoteId(){ return postVoteId; }
    public PostId postId(){ return postId; }
    public MemberId voterId(){ return voterId; }
    public int value(){ return value; }
}
