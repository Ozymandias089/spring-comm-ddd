package com.y11i.springcommddd.unit.votes;

import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.votes.domain.CommentVote;
import com.y11i.springcommddd.votes.domain.exception.InvalidVoteValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class CommentVoteTest {
    @Test
    @DisplayName("cast: 유효한 +1/-1 값으로 생성 가능")
    void cast_valid(){
        var cId = CommentId.newId();
        var mId = MemberId.newId();

        var upvote = CommentVote.cast(cId, mId, 1);
        var downvote = CommentVote.cast(cId, mId, -1);

        assertThat(upvote.value()).isEqualTo(1);
        assertThat(downvote.value()).isEqualTo(-1);
        assertThat(upvote.commentId()).isEqualTo(cId);
        assertThat(upvote.voterId()).isEqualTo(mId);
    }

    @Test
    @DisplayName("setValue: 1 또는 -1 이외의 값은 InvalidVoteValue 발생")
    void invalid_vote_value_throws() {
        var cId = CommentId.newId();
        var mId = MemberId.newId();
        var vote = CommentVote.cast(cId, mId, 1);

        assertThatThrownBy(() -> vote.setValue(0))
                .isInstanceOf(InvalidVoteValue.class)
                .hasMessageContaining("1 or -1");

        assertThatThrownBy(() -> vote.setValue(2))
                .isInstanceOf(InvalidVoteValue.class);
    }

    @Test
    @DisplayName("생성자: null commentId/voterId면 NPE")
    void null_arguments_throw_npe() {
        assertThatNullPointerException().isThrownBy(() ->
                new CommentVote(null, MemberId.newId(), 1)
        );
        assertThatNullPointerException().isThrownBy(() ->
                new CommentVote(CommentId.newId(), null, 1)
        );
    }
}
