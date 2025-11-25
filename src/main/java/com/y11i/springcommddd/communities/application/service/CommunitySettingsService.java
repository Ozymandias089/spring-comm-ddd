package com.y11i.springcommddd.communities.application.service;

import com.y11i.springcommddd.communities.application.port.in.CommunitySettingsUseCase;
import com.y11i.springcommddd.communities.application.port.internal.CommunityAuthorization;
import com.y11i.springcommddd.communities.application.port.internal.CommunityLookup;
import com.y11i.springcommddd.communities.application.port.internal.CommunityViewMapper;
import com.y11i.springcommddd.communities.application.port.out.SaveCommunityPort;
import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.CommunityRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunitySettingsService implements CommunitySettingsUseCase {
    private final CommunityLookup communityLookup;
    private final SaveCommunityPort saveCommunityPort;
    private final CommunityAuthorization communityAuthorization;
    private final CommunityViewMapper communityViewMapper;

    @Override
    @Transactional
    public CommunityId redescribe(RedescribeCommand cmd) {
        Community community = communityLookup.getByNameKeyOrThrow(cmd.nameKey());
        // 1. 권한: ADMIN 이거나 해당 커뮤니티의 MOD 여야 함
        communityAuthorization.requireAdminOrModerator(cmd.actorId(), community.communityId());
        // 2. 설명 변경
        community.redescribe(cmd.description());
        saveCommunityPort.save(community);
        log.info("Saved community {}", community.nameKey());

        return community.communityId();
    }

    @Override
    @Transactional
    public int replaceRules(ReplaceRulesCommand cmd) {
        // 1. 커뮤니티 로드
        Community community = communityLookup.getByNameKeyOrThrow(cmd.nameKey());
        // 2. 권한 검증
        communityAuthorization.requireAdminOrModerator(cmd.actorId(), community.communityId());
        // 3. DTO를 VO로 매핑
        List<CommunityRule> rules = cmd.rules().stream().map(communityViewMapper::toRuleEntity).toList();
        // 4. 규칙 교체 (List<T> -> Collection<T>)
        community.replaceRules(rules);
        // 5. 저장
        Community saved = saveCommunityPort.save(community);
        log.info("Saved community rules for {}", saved.nameKey());
        // 6. 사이즈 반환
        return saved.rules().size();
    }

    /**
     * 커뮤니티의 프로필/배너 이미지를 변경합니다.
     *
     * <p>
     * profileImageUrl, bannerImageUrl 중 null 이 아닌 필드만 변경 대상이 됩니다.
     * 둘 다 null 이면 아무 변경도 수행하지 않습니다.
     * </p>
     *
     * @param cmd 커맨드
     */
    @Override
    @Transactional
    public CommunityId changeImages(ChangeImagesCommand cmd) {
        // 1. 커뮤니티 로드
        Community community = communityLookup.getByNameKeyOrThrow(cmd.nameKey());
        log.debug("Loading Communities for {}", community.nameKey().value());
        // 2. 권한 검증
        communityAuthorization.requireAdminOrModerator(cmd.actorId(), community.communityId());

        // 3. 선택적 필드 변경
        String profile = cmd.profileImageUrl();
        String banner = cmd.bannerImageUrl();

        // null: 무시, "" : 삭제, 나머지: 새 URL 설정
        if (profile != null) {
            community.changeProfileImage(profile.isBlank() ? null : profile);
            log.info("Changed profile image for c/{}", cmd.nameKey().value());
        }
        if (banner != null) {
            community.changeBannerImage(banner.isBlank() ? null : banner);
            log.info("Changed Banner image for c/{}", cmd.nameKey().value());
        }

        Community saved = saveCommunityPort.save(community);
        log.info("Saved Images for c/{}", saved.nameKey().value());
        return saved.communityId();
    }
}
