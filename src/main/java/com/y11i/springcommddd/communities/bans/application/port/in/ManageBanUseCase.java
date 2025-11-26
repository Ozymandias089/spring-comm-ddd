package com.y11i.springcommddd.communities.bans.application.port.in;

import com.y11i.springcommddd.communities.bans.domain.CommunityBanId;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.time.Duration;

public interface ManageBanUseCase {

    /**
     * 타깃 멤버에게 커뮤니티 밴을 부여합니다.
     * - actorId: ADMIN 또는 MOD
     * - targetMemberId: 밴 대상
     * - duration이 null이면 영구 밴
     *
     * @return 생성된 커뮤니티 밴 ID
     */
    CommunityBanId banMember(BanMemberCommand cmd);

    /**
     * 타깃 멤버의 밴을 해제합니다.
     * 정책에 따라:
     * - lift()만 호출해 이력을 남기거나
     * - 실제로 delete()로 삭제할 수도 있습니다.
     */
    void unbanMember(UnbanMemberCommand cmd);

    /**
     * 밴 추가용 커맨드
     */
    record BanMemberCommand(
            MemberId actorId,             // 밴을 수행하는 관리자/모더
            CommunityNameKey nameKey,     // 대상 커뮤니티
            MemberId targetMemberId,      // 밴 당하는 멤버
            Duration duration,            // null 이면 영구 밴
            String reason                 // BanReason 으로 감싸질 원시 문자열
    ) {}

    /**
     * 밴 해제용 커맨드
     */
    record UnbanMemberCommand(
            MemberId actorId,             // 해제 수행자 (ADMIN/MOD)
            CommunityNameKey nameKey,     // 대상 커뮤니티
            MemberId targetMemberId       // 밴 해제 대상
    ) {}
}
