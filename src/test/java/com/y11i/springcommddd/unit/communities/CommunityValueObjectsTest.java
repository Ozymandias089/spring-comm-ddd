package com.y11i.springcommddd.unit.communities;

import com.y11i.springcommddd.communities.domain.*;
import com.y11i.springcommddd.communities.domain.exception.InvalidCommunityName;
import com.y11i.springcommddd.communities.domain.exception.InvalidCommunityNameKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("[Community] value objects unit tests")
class CommunityValueObjectsTest {

    // --- CommunityId ---
    @Test
    @DisplayName("CommunityId.newId: UUID가 null이 아니다")
    void community_id_new_id_not_null() {
        CommunityId id = CommunityId.newId();
        assertThat(id).isNotNull();
        assertThat(id.id()).isNotNull();
    }

    // --- CommunityName: happy path ---
    @Test
    @DisplayName("CommunityName: 트림되고 100자 이내면 생성 성공")
    void community_name_valid() {
        CommunityName name = new CommunityName("  Hello World  ");
        assertThat(name.value()).isEqualTo("Hello World");
    }

    // --- CommunityName: null/blank/too long ---
    @Test
    @DisplayName("CommunityName: null이면 InvalidCommunityName")
    void community_name_null_throws() {
        assertThatThrownBy(() -> new CommunityName(null))
                .isInstanceOf(InvalidCommunityName.class);
    }

    @Test
    @DisplayName("CommunityName: 공백이면 InvalidCommunityName")
    void community_name_blank_throws() {
        assertThatThrownBy(() -> new CommunityName("   "))
                .isInstanceOf(InvalidCommunityName.class);
    }

    @Test
    @DisplayName("CommunityName: 100자 초과면 InvalidCommunityName")
    void community_name_too_long_throws() {
        String over100 = "a".repeat(101);
        assertThatThrownBy(() -> new CommunityName(over100))
                .isInstanceOf(InvalidCommunityName.class);
    }

    // --- CommunityNameKey: normalize + pattern ---
    @Test
    @DisplayName("CommunityNameKey: 소문자/공백→_ 및 허용문자만 남긴다")
    void community_name_key_normalize() {
        CommunityNameKey key = new CommunityNameKey("  New  Cool!! Name  ");
        assertThat(key.value()).isEqualTo("new_cool_name");
    }

    @Test
    @DisplayName("CommunityNameKey: null이면 InvalidCommunityNameKey")
    void community_name_key_null_throws() {
        assertThatThrownBy(() -> new CommunityNameKey(null))
                .isInstanceOf(InvalidCommunityNameKey.class);
    }

    @Test
    @DisplayName("CommunityNameKey: 허용범위를 벗어나면 InvalidCommunityNameKey")
    void community_name_key_pattern_throws() {
        // 정규화 후 길이<3 이거나 >32가 되게 만들어 예외 유도
        assertThatThrownBy(() -> new CommunityNameKey("A")) // -> "a" (길이 1)
                .isInstanceOf(InvalidCommunityNameKey.class);

        String longRaw = "a".repeat(40);
        assertThatThrownBy(() -> new CommunityNameKey(longRaw)) // -> 길이 40
                .isInstanceOf(InvalidCommunityNameKey.class);
    }
}
