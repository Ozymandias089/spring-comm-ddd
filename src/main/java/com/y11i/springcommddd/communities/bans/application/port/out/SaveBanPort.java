package com.y11i.springcommddd.communities.bans.application.port.out;

import com.y11i.springcommddd.communities.bans.domain.CommunityBan;

public interface SaveBanPort {
    CommunityBan saveBan(CommunityBan ban);
}
