package com.y11i.springcommddd.communities.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.domain.CommunityRule;
import com.y11i.springcommddd.communities.dto.internal.CommunityRuleDTO;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.List;

public interface CommunitySettingsUseCase {
    CommunityId redescribe(RedescribeCommand cmd);

    /// TODO: IMPLEMENT THESE NUTS
//    CommunityId changeProfileImage(ChangeProfileImageCommand cmd);
//    CommunityId changeBannerImage(ChangeBannerImageCommand cmd);
    int replaceRules(ReplaceRulesCommand cmd);

    record RedescribeCommand(MemberId actorId, CommunityNameKey nameKey, String description){}
    record ChangeProfileImageCommand(CommunityNameKey nameKey, String url){}
    record ChangeBannerImageCommand(CommunityNameKey nameKey, String url){}
    record ReplaceRulesCommand(MemberId actorId, CommunityNameKey nameKey, List<CommunityRuleDTO> rules){}
}
