package com.y11i.springcommddd.unit.posts.vo;

import com.y11i.springcommddd.posts.domain.Content;
import com.y11i.springcommddd.posts.domain.Title;
import com.y11i.springcommddd.posts.domain.exception.InvalidContent;
import com.y11i.springcommddd.posts.domain.exception.InvalidTitle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class TitleContentTest {

    @Test
    @DisplayName("Title: 정상 값은 trim되고 보존")
    void title_ok() {
        var t = new Title("  Hello World  ");
        assertThat(t.value()).isEqualTo("Hello World");
    }

    @Test
    @DisplayName("Title: null/blank/201자 초과는 InvalidTitle")
    void title_invalid() {
        assertThatThrownBy(() -> new Title(null)).isInstanceOf(InvalidTitle.class);
        assertThatThrownBy(() -> new Title("   ")).isInstanceOf(InvalidTitle.class);

        var twoHundred = "x".repeat(200);
        var twoHundredOne = "x".repeat(201);
        assertThat(new Title(twoHundred).value()).hasSize(200);
        assertThatThrownBy(() -> new Title(twoHundredOne)).isInstanceOf(InvalidTitle.class);
    }

    @Test
    @DisplayName("Content: 정상 값은 생성 OK")
    void content_ok() {
        var c = new Content("body");
        assertThat(c.value()).isEqualTo("body");
    }

    @Test
    @DisplayName("Content: null/blank는 InvalidContent")
    void content_invalid() {
        assertThatThrownBy(() -> new Content(null)).isInstanceOf(InvalidContent.class);
        assertThatThrownBy(() -> new Content("   ")).isInstanceOf(InvalidContent.class);
    }
}
