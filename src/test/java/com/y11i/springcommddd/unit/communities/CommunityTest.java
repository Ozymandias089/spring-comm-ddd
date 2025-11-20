package com.y11i.springcommddd.unit.communities;

import com.y11i.springcommddd.communities.domain.*;
import com.y11i.springcommddd.communities.domain.exception.CommunityArchivedModificationNotAllowed;
import com.y11i.springcommddd.communities.domain.exception.CommunityStatusTransitionNotAllowed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("[Community] aggregate unit tests")
class CommunityTest {

    // --- Happy path: create ---
    @Test
    @DisplayName("create: ACTIVE로 생성되고 nameKey가 정규화되어 설정된다")
    void create_active_and_key_normalized() {
        Community c = Community.create("My Cool Community!!", "My Cool Community Description");
        assertThat(c.status()).isEqualTo(CommunityStatus.ACTIVE);
        assertThat(c.communityName().value()).isEqualTo("My Cool Community!!");
        assertThat(c.nameKey().value()).isEqualTo("my_cool_community");
    }

    // --- Happy path: rename ---
    @Test
    @DisplayName("rename: ACTIVE 상태에서 이름 변경 시 name과 nameKey가 함께 갱신된다")
    void rename_when_active_updates_name_and_key() {
        Community c = Community.create("Alpha Team", "Alpha Team Description");
        c.rename("New Name 2025!");
        assertThat(c.communityName().value()).isEqualTo("New Name 2025!");
        assertThat(c.nameKey().value()).isEqualTo("new_name_2025");
    }

    // --- Guard: ARCHIVED에서 rename 금지 ---
    @Test
    @DisplayName("rename: ARCHIVED 상태에서 변경 시 CommunityArchivedModificationNotAllowed")
    void rename_when_archived_throws() {
        Community c = Community.create("Alpha Team", "Alpha Team Description");
        c.archive();
        assertThatThrownBy(() -> c.rename("new"))
                .isInstanceOf(CommunityArchivedModificationNotAllowed.class)
                .hasMessageContaining("Archived");
    }

    // --- State transition: archive ---
    @Test
    @DisplayName("archive: ACTIVE -> ARCHIVED 전환")
    void archive_changes_status_to_archived() {
        Community c = Community.create("Alpha Team", "Alpha Team Description");
        c.archive();
        assertThat(c.status()).isEqualTo(CommunityStatus.ARCHIVED);
    }

    // --- archive 두 번 호출해도 ARCHIVED 유지 ---
    @Test
    @DisplayName("archive: ARCHIVED에서 다시 archive 호출해도 ARCHIVED 유지")
    void archive_twice_stays_archived() {
        Community c = Community.create("Alpha Team", "Alpha Team Description");
        c.archive();
        c.archive(); // no-op
        assertThat(c.status()).isEqualTo(CommunityStatus.ARCHIVED);
    }

    // --- State transition: restore ---
    @Test
    @DisplayName("restore: ARCHIVED -> ACTIVE 전환")
    void restore_from_archived_to_active() {
        Community c = Community.create("Alpha Team", "Alpha Team Description");
        c.archive();
        c.restore();
        assertThat(c.status()).isEqualTo(CommunityStatus.ACTIVE);
    }

    // --- Guard: ARCHIVED가 아니면 restore 불가 ---
    @Test
    @DisplayName("restore: ARCHIVED가 아니면 CommunityStatusTransitionNotAllowed")
    void restore_when_not_archived_throws() {
        Community c = Community.create("Alpha Team","Alpha Team Description"); // ACTIVE
        assertThatThrownBy(c::restore)
                .isInstanceOf(CommunityStatusTransitionNotAllowed.class)
                .hasMessageContaining("Only ARCHIVED");
    }

    // --- Profile image change (ACTIVE ok) ---
    @Test
    @DisplayName("changeProfileImage: ACTIVE 상태에서는 설정/해제가 가능하다(null 허용)")
    void change_profile_image_active() {
        Community c = Community.create("Alpha Team", "Alpha Team Description");
        c.changeProfileImage("https://img.example.com/p.png");
        assertThat(c.profileImage()).isNotNull();
        assertThat(c.profileImage().value()).isEqualTo("https://img.example.com/p.png");
        c.changeProfileImage(null);
        assertThat(c.profileImage()).isNull();
    }

    // --- Profile image change (ARCHIVED blocked) ---
    @Test
    @DisplayName("changeProfileImage: ARCHIVED 상태에서는 금지")
    void change_profile_image_archived_throws() {
        Community c = Community.create("Alpha Team", "Alpha Team Description");
        c.archive();
        assertThatThrownBy(() -> c.changeProfileImage("https://x"))
                .isInstanceOf(CommunityArchivedModificationNotAllowed.class);
    }

    // --- Banner image change (ACTIVE ok) ---
    @Test
    @DisplayName("changeBannerImage: ACTIVE 상태에서는 설정/해제가 가능하다(null 허용)")
    void change_banner_image_active() {
        Community c = Community.create("Alpha Team", "Alpha Team Description");
        c.changeBannerImage("https://img.example.com/b.png");
        assertThat(c.bannerImage()).isNotNull();
        assertThat(c.bannerImage().value()).isEqualTo("https://img.example.com/b.png");
        c.changeBannerImage(null);
        assertThat(c.bannerImage()).isNull();
    }

    // --- Banner image change (ARCHIVED blocked) ---
    @Test
    @DisplayName("changeBannerImage: ARCHIVED 상태에서는 금지")
    void change_banner_image_archived_throws() {
        Community c = Community.create("Alpha Team", "Alpha Team Description");
        c.archive();
        assertThatThrownBy(() -> c.changeBannerImage("https://x"))
                .isInstanceOf(CommunityArchivedModificationNotAllowed.class);
    }
}
