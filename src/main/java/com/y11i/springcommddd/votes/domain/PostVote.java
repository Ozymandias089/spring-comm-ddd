package com.y11i.springcommddd.votes.domain;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.shared.domain.AggregateRoot;
import jakarta.persistence.*;

import java.util.Objects;

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

    @Column(name = "value", nullable = false)
    private int value;

    protected PostVote() {}

    public PostVote(PostId postId, MemberId voterId, int value) {
        this.postVoteId = PostVoteId.newId();
        this.postId = Objects.requireNonNull(postId);
        this.voterId = Objects.requireNonNull(voterId);
        setValue(value);
    }

    public static PostVote cast(PostId postId, MemberId voterId, int value) {
        return new PostVote(postId, voterId, value);
    }

    public void setValue(int v) {
        if (v != 1 && v != -1) throw new IllegalArgumentException("vote must be 1 or -1");
        this.value = v;
    }

    // accessors
    public PostVoteId postVoteId(){ return postVoteId; }
    public PostId postId(){ return postId; }
    public MemberId voterId(){ return voterId; }
    public int value(){ return value; }
}
