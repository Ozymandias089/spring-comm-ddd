package com.y11i.springcommddd.communities.bans.application.service;

import com.y11i.springcommddd.communities.application.port.internal.CommunityAuthorization;
import com.y11i.springcommddd.communities.application.port.internal.CommunityLookup;
import com.y11i.springcommddd.communities.bans.application.port.in.ManageBanUseCase;
import com.y11i.springcommddd.communities.bans.application.port.out.LoadBanPort;
import com.y11i.springcommddd.communities.bans.application.port.out.SaveBanPort;
import com.y11i.springcommddd.communities.bans.domain.BanReason;
import com.y11i.springcommddd.communities.bans.domain.CommunityBan;
import com.y11i.springcommddd.communities.bans.domain.CommunityBanId;
import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ManageBanService implements ManageBanUseCase {

    private final LoadBanPort loadBanPort;
    private final SaveBanPort saveBanPort;
    private final CommunityLookup communityLookup;
    private final CommunityAuthorization communityAuthorization;

    // ───────────────────────────────── 커맨드(use case) ─────────────────────────────────

    /**
     * 타깃 멤버에게 커뮤니티 밴을 부여합니다.
     * - actorId: ADMIN 또는 MOD
     * - targetMemberId: 밴 대상
     * - duration이 null이면 영구 밴
     */
    @Override
    @Transactional
    public CommunityBanId banMember(BanMemberCommand cmd) {
        // 1. 커뮤니티 + 권한 검증
        Community community = loadCommunityAndAuthorize(cmd.nameKey().value(), cmd.actorId());

        // 2. 이미 활성 밴이 있는지 확인 (멱등성)
        var existingActive = loadBanPort.loadActiveBan(community.communityId(), cmd.targetMemberId());
        if (existingActive.isPresent()) {
            CommunityBan existing = existingActive.get();
            log.info("Member {} is already banned from c/{} (banId={})",
                    cmd.targetMemberId().stringify(),
                    community.nameKey().value(),
                    existing.banId().stringify());
            return existing.banId();
        }

        // 3. 사유 VO 생성
        BanReason reason = new BanReason(cmd.reason());

        // 4. 도메인 팩토리로 Ban 생성
        CommunityBan ban = (cmd.duration() == null)
                ? CommunityBan.permanentBan(
                community.communityId(),
                cmd.targetMemberId(),
                cmd.actorId(),
                reason
        )
                : CommunityBan.temporaryBan(
                community.communityId(),
                cmd.targetMemberId(),
                cmd.actorId(),
                reason,
                cmd.duration()
        );

        // 5. 저장
        CommunityBan saved = saveBanPort.saveBan(ban);

        log.info("Banned member {} from c/{} by {} (banId={})",
                saved.bannedMemberId().stringify(),
                community.nameKey().value(),
                saved.processorId().stringify(),
                saved.banId().stringify());

        return saved.banId();
    }

    /**
     * 타깃 멤버의 밴을 해제합니다.
     * - lift()만 호출해 이력을 남기는 정책 유지
     */
    @Override
    @Transactional
    public void unbanMember(UnbanMemberCommand cmd) {
        // 1. 커뮤니티 + 권한 검증
        Community community = loadCommunityAndAuthorize(cmd.nameKey().value(), cmd.actorId());

        // 2. 활성 밴 찾기
        var activeBanOpt = loadBanPort.loadActiveBan(community.communityId(), cmd.targetMemberId());
        if (activeBanOpt.isEmpty()) {
            log.info("No active ban found for member {} in c/{}; nothing to unban",
                    cmd.targetMemberId().stringify(),
                    community.nameKey().value());
            return;
        }

        CommunityBan ban = activeBanOpt.get();

        // 3. 해제 도메인 로직 호출
        ban.lift(cmd.actorId());

        // 4. 저장 (이력 남기기)
        saveBanPort.saveBan(ban);

        log.info("Unbanned member {} from c/{} by {} (banId={})",
                ban.bannedMemberId().stringify(),
                community.nameKey().value(),
                cmd.actorId().stringify(),
                ban.banId().stringify());
    }
    /**
     * nameKey로 커뮤니티를 로드하고, ADMIN 또는 해당 커뮤니티 MOD 권한을 요구한다.
     * (조회/명령 양쪽에서 공통으로 사용)
     */
    private Community loadCommunityAndAuthorize(String nameKey, MemberId actorId) {
        Community community = communityLookup.getByNameKeyOrThrow(new CommunityNameKey(nameKey));
        communityAuthorization.requireAdminOrModerator(actorId, community.communityId());
        return community;
    }
}
