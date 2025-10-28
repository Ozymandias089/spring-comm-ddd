package com.y11i.springcommddd.unit.posts.media;

import com.y11i.springcommddd.posts.media.domain.Url;
import com.y11i.springcommddd.posts.media.domain.exception.InvalidUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class UrlTest {
    @Test
    @DisplayName("정상 URL이면 생성 성공하고 trim 적용")
    void valid_ok() {
        var u = new Url("  https://cdn/x.jpg  ");
        assertThat(u.value()).isEqualTo("https://cdn/x.jpg");
    }

    @Test
    @DisplayName("null/blank는 InvalidUrl")
    void invalid_nullOrBlank() {
        assertThatThrownBy(() -> new Url(null)).isInstanceOf(InvalidUrl.class);
        assertThatThrownBy(() -> new Url("  ")).isInstanceOf(InvalidUrl.class);
    }
}
