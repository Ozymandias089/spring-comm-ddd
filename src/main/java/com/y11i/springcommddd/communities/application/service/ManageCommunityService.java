package com.y11i.springcommddd.communities.application.service;

import com.y11i.springcommddd.communities.application.port.in.ManageCommunityUseCase;
import com.y11i.springcommddd.communities.application.port.out.LoadCommunityPort;
import com.y11i.springcommddd.communities.application.port.out.LoadMemberForCommunityPort;
import com.y11i.springcommddd.communities.application.port.out.SaveCommunityPort;
import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.exception.CommunityNotFound;
import com.y11i.springcommddd.communities.domain.exception.CommunityStatusTransitionNotAllowed;
import com.y11i.springcommddd.iam.domain.Member;
import com.y11i.springcommddd.iam.domain.MemberId;
import com.y11i.springcommddd.iam.domain.MemberRole;
import com.y11i.springcommddd.iam.domain.exception.MemberNotFound;
import com.y11i.springcommddd.iam.domain.exception.UnauthorizedMemberAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 커뮤니티 관리(관리자용) 유즈케이스 구현체.
 *
 * <p><b>역할</b></p>
 * <ul>
 *     <li>관리자(Admin)가 커뮤니티 상태를 변경(활성화 등)할 수 있는 기능 제공</li>
 *     <li>멤버 권한 검증 및 커뮤니티 로딩/저장을 조율하는 애플리케이션 서비스</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ManageCommunityService implements ManageCommunityUseCase {
    private final LoadCommunityPort loadCommunityPort;
    private final LoadMemberForCommunityPort loadMemberForCommunityPort;
    private final SaveCommunityPort saveCommunityPort;

    /**
     * PENDING 상태의 커뮤니티를 ACTIVE 상태로 활성화합니다.
     *
     * <p><b>전제조건</b></p>
     * <ul>
     *     <li>요청자는 {@link MemberRole#ADMIN} 권한을 가져야 함</li>
     *     <li>커뮤니티는 존재해야 하며, 현재 상태가 PENDING 이어야 함</li>
     * </ul>
     *
     * @param cmd 액터 ID와 커뮤니티 ID를 담은 커맨드
     * @return 활성화된 커뮤니티 ID
     *
     * @throws MemberNotFound          액터 멤버를 찾을 수 없는 경우
     * @throws UnauthorizedMemberAction ADMIN 권한이 없는 경우
     * @throws CommunityNotFound       커뮤니티를 찾을 수 없는 경우
     * @throws CommunityStatusTransitionNotAllowed 커뮤니티 상태 전환이 허용되지 않는 경우
     */
    @Override
    @Transactional
    public CommunityId activateCommunity(ActivateCommunityCommand cmd) {
        // 1. 권한 검증
        ensurePermission(cmd.actorId(), MemberRole.ADMIN);
        // 2. 커뮤니티 로드
        Community community = loadCommunityPort.loadById(cmd.communityId())
                .orElseThrow(() -> new CommunityNotFound("Community not found"));
        // 3. 권한 변경 (PENDING -> ACTIVE)
        community.activate();
        // 4. 저장
        Community saved = saveCommunityPort.save(community);
        // 5. 반환
        return saved.communityId();
    }

    /**
     * 액터가 특정 역할을 가지고 있는지 검증합니다.
     *
     * @param actorId 권한을 검증할 멤버 ID
     * @param role    요구되는 역할
     *
     * @throws MemberNotFound          멤버를 찾을 수 없는 경우
     * @throws UnauthorizedMemberAction 요구된 역할을 가지고 있지 않은 경우
     */
    private void ensurePermission(MemberId actorId, MemberRole role) {
        Member member = loadMemberForCommunityPort.loadById(actorId).orElseThrow(() -> new MemberNotFound("Member not found"));

        if (!member.hasRole(role))
            throw new UnauthorizedMemberAction("Action not allowed");
    }
}
