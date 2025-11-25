package com.y11i.springcommddd.communities.application.port.in;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.dto.internal.CommunityRuleDTO;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.List;

public interface CommunitySettingsUseCase {
    CommunityId redescribe(RedescribeCommand cmd);
    int replaceRules(ReplaceRulesCommand cmd);
    /**
     * 커뮤니티의 프로필/배너 이미지를 변경합니다.
     *
     * <p>
     * profileImageUrl, bannerImageUrl 중 null 이 아닌 필드만 변경 대상이 됩니다.
     * 둘 다 null 이면 아무 변경도 수행하지 않습니다.
     * </p>
     */
    CommunityId changeImages(ChangeImagesCommand cmd);

    record RedescribeCommand(MemberId actorId, CommunityNameKey nameKey, String description){}
    record ReplaceRulesCommand(MemberId actorId, CommunityNameKey nameKey, List<CommunityRuleDTO> rules){}
    record ChangeImagesCommand(
            MemberId actorId,
            CommunityNameKey nameKey,
            String profileImageUrl,
            String bannerImageUrl
    ) {}
}
