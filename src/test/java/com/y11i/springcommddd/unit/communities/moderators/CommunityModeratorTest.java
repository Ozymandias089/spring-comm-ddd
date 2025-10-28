package com.y11i.springcommddd.unit.communities.moderators;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModeratorId;
import com.y11i.springcommddd.iam.domain.MemberId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * [CommunityModerator] aggregate unit tests.
 *
 * 순수 도메인 단위 테스트: JPA/Auditing 미사용.
 * - grant() 성공/실패 (null 검증)
 * - ID 생성 보장
 * - grantedAt은 Auditing 필드이므로 순수 도메인에서는 null임을 확인
 */
@DisplayName("[CommunityModerator] aggregate unit tests")
class CommunityModeratorTest {

    // --- Happy path: grant ---
    @Test
    @DisplayName("grant: communityId/memberId로 모더레이터를 생성하면 ID가 발급되고 필드가 설정된다")
    void grant_success() {
        // Given
        var communityId = CommunityId.newId();
        var memberId = MemberId.newId();

        // When
        var mod = CommunityModerator.grant(communityId, memberId);

        // Then
        assertThat(mod.id()).isNotNull();
        assertThat(mod.id().id()).isNotNull();
        assertThat(mod.communityId()).isEqualTo(communityId);
        assertThat(mod.memberId()).isEqualTo(memberId);

        // Auditing 필드: 순수 도메인 테스트에서는 null (JPA가 채워줌)
        assertThat(mod.grantedAt()).isNull();
    }

    // --- Guard: null communityId ---
    @Test
    @DisplayName("grant: communityId가 null이면 NPE")
    void grant_null_community_throws() {
        var memberId = MemberId.newId();
        assertThatThrownBy(() -> CommunityModerator.grant(null, memberId))
                .isInstanceOf(NullPointerException.class);
    }

    // --- Guard: null memberId ---
    @Test
    @DisplayName("grant: memberId가 null이면 NPE")
    void grant_null_member_throws() {
        var communityId = CommunityId.newId();
        assertThatThrownBy(() -> CommunityModerator.grant(communityId, null))
                .isInstanceOf(NullPointerException.class);
    }

    // --- ID uniqueness (통계적 보장 수준 확인) ---
    @Test
    @DisplayName("grant: 서로 다른 grant 호출은 서로 다른 CommunityModeratorId를 갖는다")
    void grant_ids_are_unique() {
        var c = CommunityId.newId();
        var m1 = MemberId.newId();
        var m2 = MemberId.newId();

        var mod1 = CommunityModerator.grant(c, m1);
        var mod2 = CommunityModerator.grant(c, m2);

        assertThat(mod1.id()).isNotEqualTo(mod2.id());
        assertThat(mod1.id().id()).isNotEqualTo(mod2.id().id());
    }

    // --- Value Object: CommunityModeratorId ---
    @Test
    @DisplayName("CommunityModeratorId: newId()는 null이 아닌 UUID를 생성한다")
    void communityModeratorId_newId() {
        CommunityModeratorId id = CommunityModeratorId.newId();
        assertThat(id).isNotNull();
        assertThat(id.id()).isNotNull();
    }

    @Test
    @DisplayName("CommunityModeratorId: null UUID로 생성하면 NPE")
    void communityModeratorId_null_throws() {
        assertThatThrownBy(() -> new CommunityModeratorId(null))
                .isInstanceOf(NullPointerException.class);
    }
}
