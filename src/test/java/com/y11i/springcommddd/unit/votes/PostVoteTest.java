package com.y11i.springcommddd.unit.votes;

import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.domain.PostId;
import com.y11i.springcommddd.votes.domain.PostVote;
import com.y11i.springcommddd.votes.domain.exception.InvalidVoteValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PostVoteTest {

    @Test
    @DisplayName("cast: 유효한 +1/-1 값으로 생성 가능")
    void cast_valid() {
        var pId = PostId.newId();
        var mId = MemberId.newId();

        var up = PostVote.cast(pId, mId, 1);
        var down = PostVote.cast(pId, mId, -1);

        assertThat(up.value()).isEqualTo(1);
        assertThat(down.value()).isEqualTo(-1);
        assertThat(up.postId()).isEqualTo(pId);
        assertThat(up.voterId()).isEqualTo(mId);
    }

    @Test
    @DisplayName("setValue: 1/-1 외에는 InvalidVoteValue 발생")
    void invalid_value_throws() {
        var v = PostVote.cast(PostId.newId(), MemberId.newId(), 1);
        assertThatThrownBy(() -> v.setValue(0))
                .isInstanceOf(InvalidVoteValue.class);
        assertThatThrownBy(() -> v.setValue(999))
                .isInstanceOf(InvalidVoteValue.class);
    }

    @Test
    @DisplayName("생성자: null postId/voterId면 NPE")
    void null_args_throw_npe() {
        assertThatNullPointerException().isThrownBy(() ->
                new PostVote(null, MemberId.newId(), 1)
        );
        assertThatNullPointerException().isThrownBy(() ->
                new PostVote(PostId.newId(), null, 1)
        );
    }
}
