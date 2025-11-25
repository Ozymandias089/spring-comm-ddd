package com.y11i.springcommddd.communities.application.service;

import com.y11i.springcommddd.communities.application.port.in.CommunitySettingsUseCase;
import com.y11i.springcommddd.communities.application.port.out.LoadCommunityModeratorsPort;
import com.y11i.springcommddd.communities.application.port.out.LoadCommunityPort;
import com.y11i.springcommddd.communities.application.port.out.LoadMemberForCommunityPort;
import com.y11i.springcommddd.communities.application.port.out.SaveCommunityPort;
import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.CommunityRule;
import com.y11i.springcommddd.communities.domain.exception.CommunityNotFound;
import com.y11i.springcommddd.communities.dto.internal.CommunityRuleDTO;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.MemberRole;
import com.y11i.springcommddd.iam.domain.exception.MemberNotFound;
import com.y11i.springcommddd.iam.domain.exception.UnauthorizedMemberAction;
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
    private final LoadCommunityPort loadCommunityPort;
    private final SaveCommunityPort saveCommunityPort;
    private final LoadMemberForCommunityPort loadMemberForCommunityPort;
    private final LoadCommunityModeratorsPort loadCommunityModeratorsPort;

    @Override
    @Transactional
    public CommunityId redescribe(RedescribeCommand cmd) {
        Community community = loadCommunityPort.loadByNameKey(cmd.nameKey()).orElseThrow(() -> new CommunityNotFound("Community not found"));
        // 1. 권한: ADMIN 이거나 해당 커뮤니티의 MOD 여야 함
        ensureAdminOrModerator(cmd.actorId(), community.communityId());
        // 2. 설명 변경
        community.redescribe(cmd.description());
        saveCommunityPort.save(community);
        //noinspection LoggingSimilarMessage
        log.info("Saved community {}", community.nameKey());

        return community.communityId();
    }

    @Override
    @Transactional
    public int replaceRules(ReplaceRulesCommand cmd) {
        // 1. 커뮤니티 로드
        Community community = loadCommunityPort.loadByNameKey(cmd.nameKey()).orElseThrow(() -> new CommunityNotFound("Community not found"));
        // 2. 권한 검증
        ensureAdminOrModerator(cmd.actorId(), community.communityId());
        // 3. DTO를 VO로 매핑
        List<CommunityRule> rules = cmd.rules().stream().map(this::toCommunityRuleVO).toList();
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
        Community community = loadCommunityPort.loadByNameKey(cmd.nameKey()).orElseThrow(() -> new CommunityNotFound("Community not found"));
        log.debug("Loading Communities for {}", community.nameKey().value());
        // 2. 권한 검증
        ensureAdminOrModerator(cmd.actorId(), community.communityId());

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

    private CommunityRule toCommunityRuleVO(CommunityRuleDTO dto) {
        return new CommunityRule(dto.title(), dto.description(), dto.displayOrder());
    }

    private void ensureAdminOrModerator(MemberId actorId, CommunityId communityId) {
        Member member = loadMemberForCommunityPort.loadById(actorId).orElseThrow(() -> new MemberNotFound("Member not found"));

        // 1) ADMIN이면 통과
        if (member.hasRole(MemberRole.ADMIN)) return;

        // 2) 아니면 해당 커뮤니티의 모더레이터인지 확인
        List<CommunityModerator> moderators = loadCommunityModeratorsPort.loadByCommunityId(communityId);

        boolean isModerator = moderators.stream()
                .anyMatch(m -> m.memberId().equals(actorId));

        if (!isModerator) {
            log.error("Member {} is neither Admin nor Moderator", actorId);
            throw new UnauthorizedMemberAction("This action is only allowed for admin or community moderators");
        }
    }
}
