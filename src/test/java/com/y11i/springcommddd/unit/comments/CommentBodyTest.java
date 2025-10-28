package com.y11i.springcommddd.unit.comments;

import com.y11i.springcommddd.comments.domain.CommentBody;
import com.y11i.springcommddd.comments.domain.exception.InvalidCommentBody;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CommentBodyTest {

    @Test
    @DisplayName("정상 생성: value 유지 및 equals/hashCode 동작")
    void create_and_equals() {
        var a = new CommentBody(" hello ");
        var b = new CommentBody(" hello ");
        assertThat(a.value()).isEqualTo(" hello ");
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("InvalidCommentBody: null/blank인 경우 예외")
    void invalid_body_throws() {
        assertThatThrownBy(() -> new CommentBody(null))
                .isInstanceOf(InvalidCommentBody.class);
        assertThatThrownBy(() -> new CommentBody("   "))
                .isInstanceOf(InvalidCommentBody.class);
    }
}
