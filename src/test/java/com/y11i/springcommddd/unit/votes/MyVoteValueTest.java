package com.y11i.springcommddd.unit.votes;

import com.y11i.springcommddd.comments.domain.CommentId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.votes.domain.MyCommentVote;
import com.y11i.springcommddd.votes.domain.MyPostVote;
import com.y11i.springcommddd.votes.domain.exception.InvalidVoteValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class MyVoteValueTest {

    @Test
    @DisplayName("MyCommentVote/MyPostVote: +1/-1만 허용")
    void valid_vote_values() {
        var cId = CommentId.newId();
        var pId = PostId.newId();

        var upC = new MyCommentVote(cId, 1);
        var downC = new MyCommentVote(cId, -1);
        var upP = new MyPostVote(pId, 1);
        var downP = new MyPostVote(pId, -1);

        assertThat(upC.value()).isEqualTo(1);
        assertThat(downC.value()).isEqualTo(-1);
        assertThat(upP.value()).isEqualTo(1);
        assertThat(downP.value()).isEqualTo(-1);
    }

    @Test
    @DisplayName("잘못된 값(0, 2 등)은 InvalidVoteValue 예외 발생")
    void invalid_vote_values_throw() {
        var cId = CommentId.newId();
        var pId = PostId.newId();

        assertThatThrownBy(() -> new MyCommentVote(cId, 0))
                .isInstanceOf(InvalidVoteValue.class);
        assertThatThrownBy(() -> new MyPostVote(pId, 99))
                .isInstanceOf(InvalidVoteValue.class);
    }
}
