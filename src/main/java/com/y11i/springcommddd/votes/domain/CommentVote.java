package com.y11i.springcommddd.votes.domain;

import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.shared.domain.AggregateRoot;
import jakarta.persistence.*;

import java.util.Objects;

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

    @Column(name="value", nullable=false) // +1 or -1
    private int value;

    protected CommentVote() {}
    public CommentVote(CommentId commentId, MemberId voterId, int value) {
        this.commentVoteId = CommentVoteId.newId();
        this.commentId = Objects.requireNonNull(commentId);
        this.voterId = Objects.requireNonNull(voterId);
        setValue(value);
    }

    public static CommentVote cast(CommentId commentId, MemberId voterId, int value) {
        return new CommentVote(commentId, voterId, value);
    }

    public void setValue(int v){
        if (v != 1 && v != -1) throw new IllegalArgumentException("vote must be 1 or -1");
        this.value = v;
    }

    // accessors
    public CommentVoteId commentVoteId(){ return commentVoteId; }
    public CommentId commentId(){ return commentId; }
    public MemberId voterId(){ return voterId; }
    public int value(){ return value; }
}
