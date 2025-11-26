package com.y11i.springcommddd.communities.bans.infrastructure;

import com.y11i.springcommddd.communities.bans.application.port.out.LoadBanPort;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.posts.application.port.out.CheckCommunityBanPort;
import com.y11i.springcommddd.posts.domain.exception.MemberBannedFromCommunity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityBanCheckAdapter implements CheckCommunityBanPort {
    private final LoadBanPort loadBanPort;

    @Override
    public void ensureNotBanned(CommunityId communityId, MemberId memberId) {
        // 활성 밴 존재 여부만 확인
        boolean banned = loadBanPort.loadActiveBan(communityId, memberId).isPresent();

        if (banned) {
            // 필요하면 밴 사유, 만료일 등도 메시지에 포함 가능
            throw new MemberBannedFromCommunity("Member " + memberId.stringify()
                    + " is banned from community " + communityId.stringify());
        }
    }
}
