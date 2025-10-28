package com.y11i.springcommddd.votes.domain;

import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.shared.domain.AggregateRoot;
import com.y11i.springcommddd.votes.domain.exception.InvalidVoteValue;
import jakarta.persistence.*;

import java.util.Objects;

/**
 * 댓글 투표(CommentVote) 애그리게잇 루트.
 *
 * <p><b>개요</b><br>
 * 특정 댓글({@link CommentId})에 대해 특정 회원({@link MemberId})이 남긴
 * 단일 투표(값: +1 또는 -1)를 표현합니다.
 * </p>
 *
 * <p><b>영속성/제약</b></p>
 * <ul>
 *   <li>테이블: {@code comment_votes}</li>
 *   <li>유니크 제약: {@code uk_comment_votes_target_voter} (comment_id, voter_id)</li>
 *   <li>PK: {@link CommentVoteId} (UUID)</li>
 * </ul>
 *
 * <p><b>규칙</b></p>
 * <ul>
 *   <li>투표값은 +1 또는 -1만 허용</li>
 *   <li>동일 회원은 동일 댓글에 하나의 투표만 보유</li>
 * </ul>
 */
@Entity
@Table(name = "comment_votes",
        uniqueConstraints = @UniqueConstraint(name = "uk_comment_votes_target_voter", columnNames = {"comment_id","voter_id"}))
@Access(AccessType.FIELD)
public class CommentVote implements AggregateRoot {

    @EmbeddedId
    private CommentVoteId commentVoteId;

    @Embedded
    @AttributeOverride(name="id", column=@Column(name="comment_id", columnDefinition="BINARY(16)", nullable=false, updatable=false))
    private CommentId commentId;

    @Embedded
    @AttributeOverride(name="id", column=@Column(name="voter_id", columnDefinition="BINARY(16)", nullable=false, updatable=false))
    private MemberId voterId;

    /** 투표 값: +1(추천), -1(비추천) */
    @Column(name="value", nullable=false) // +1 or -1
    private int value;

    protected CommentVote() {}

    /**
     * 내부 생성자: 투표를 생성합니다.
     *
     * @param commentId 댓글 식별자
     * @param voterId   투표자 식별자
     * @param value     투표값 (+1 또는 -1)
     * @throws NullPointerException commentId/voterId가 null인 경우
     * @throws IllegalArgumentException value가 +1/-1 이외인 경우
     */
    public CommentVote(CommentId commentId, MemberId voterId, int value) {
        this.commentVoteId = CommentVoteId.newId();
        this.commentId = Objects.requireNonNull(commentId);
        this.voterId = Objects.requireNonNull(voterId);
        setValue(value);
    }

    // -----------------------------------------------------
    // 생성 섹션 (정적 팩토리)
    // -----------------------------------------------------

    /**
     * 투표를 생성합니다.
     *
     * @param commentId 댓글 식별자
     * @param voterId   투표자 식별자
     * @param value     투표값 (+1 또는 -1)
     */
    public static CommentVote cast(CommentId commentId, MemberId voterId, int value) {
        return new CommentVote(commentId, voterId, value);
    }

    // -----------------------------------------------------
    // 도메인 동작 섹션
    // -----------------------------------------------------

    /**
     * 투표값을 설정합니다.
     *
     * @param v +1(추천) 또는 -1(비추천)
     * @throws InvalidVoteValue 허용되지 않은 값
     */
    public void setValue(int v){
        if (v != 1 && v != -1) throw new InvalidVoteValue("vote must be 1 or -1");
        this.value = v;
    }

    // -----------------------------------------------------
    // 접근자 섹션
    // -----------------------------------------------------

    public CommentVoteId commentVoteId(){ return commentVoteId; }
    public CommentId commentId(){ return commentId; }
    public MemberId voterId(){ return voterId; }
    public int value(){ return value; }
}
