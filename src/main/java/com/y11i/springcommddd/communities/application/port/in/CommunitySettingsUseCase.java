package com.y11i.springcommddd.communities.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.domain.CommunityRule;
import com.y11i.springcommddd.communities.dto.response.CommunityRulesResponseDTO;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.List;

public interface CommunitySettingsUseCase {
    CommunityId redescribe(RedescribeCommand cmd);
//    CommunityId changeProfileImage(ChangeProfileImageCommand cmd);
//    CommunityId changeBannerImage(ChangeBannerImageCommand cmd);
//    CommunityRulesResponseDTO getRules(GetRulesCommand cmd);
//    CommunityRulesResponseDTO replaceRulesCommand(ReplaceRulesCommand cmd);

    record RedescribeCommand(MemberId actorId, CommunityNameKey nameKey, String description){}
    record ChangeProfileImageCommand(CommunityNameKey nameKey, String url){}
    record ChangeBannerImageCommand(CommunityNameKey nameKey, String url){}
    record GetRulesCommand(CommunityNameKey nameKey){}
    record ReplaceRulesCommand(CommunityNameKey nameKey, List<CommunityRule> rules){}
}
