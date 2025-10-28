package com.y11i.springcommddd.unit.shared;

import com.y11i.springcommddd.shared.domain.ImageUrl;
import com.y11i.springcommddd.shared.domain.exception.InvalidImageUrl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("[Shared] ImageUrl value object unit tests")
class ImageUrlTest {

    // --- Happy path ---
    @Test
    @DisplayName("정상적인 http/https URL은 생성 성공")
    void valid_urls_success() {
        ImageUrl u1 = new ImageUrl("http://example.com/image.png");
        ImageUrl u2 = new ImageUrl("https://cdn.example.org/pic.jpg");

        assertThat(u1.value()).isEqualTo("http://example.com/image.png");
        assertThat(u2.value()).isEqualTo("https://cdn.example.org/pic.jpg");
    }

    // --- Trim 검증 ---
    @Test
    @DisplayName("앞뒤 공백은 자동 제거된다")
    void trim_spaces_success() {
        ImageUrl u = new ImageUrl("  https://example.com/a.png  ");
        assertThat(u.value()).isEqualTo("https://example.com/a.png");
    }

    // --- Invalid: null / blank ---
    @Test
    @DisplayName("null 또는 공백 문자열은 InvalidImageUrl 발생")
    void null_or_blank_throws() {
        assertThatThrownBy(() -> new ImageUrl(null))
                .isInstanceOf(InvalidImageUrl.class)
                .hasMessageContaining("null or blank");

        assertThatThrownBy(() -> new ImageUrl("   "))
                .isInstanceOf(InvalidImageUrl.class)
                .hasMessageContaining("null or blank");
    }

    // --- Invalid: not starting with http/https ---
    @Test
    @DisplayName("http/https로 시작하지 않으면 InvalidImageUrl 발생")
    void invalid_protocol_throws() {
        assertThatThrownBy(() -> new ImageUrl("ftp://example.com"))
                .isInstanceOf(InvalidImageUrl.class)
                .hasMessageContaining("http");
    }

    // --- Invalid: contains spaces ---
    @Test
    @DisplayName("URL에 공백이 포함되어 있으면 InvalidImageUrl 발생")
    void space_in_url_throws() {
        assertThatThrownBy(() -> new ImageUrl("https://example.com/my file.png"))
                .isInstanceOf(InvalidImageUrl.class)
                .hasMessageContaining("spaces");
    }

    // --- Equality ---
    @Test
    @DisplayName("동일 문자열은 동등하다")
    void equality_based_on_value() {
        ImageUrl a = new ImageUrl("https://a.com/x.png");
        ImageUrl b = new ImageUrl("https://a.com/x.png");
        ImageUrl c = new ImageUrl("https://a.com/y.png");

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
