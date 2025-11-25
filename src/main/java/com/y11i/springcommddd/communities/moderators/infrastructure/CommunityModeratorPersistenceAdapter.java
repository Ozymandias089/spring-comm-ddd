package com.y11i.springcommddd.communities.moderators.infrastructure;

import com.y11i.springcommddd.communities.application.port.out.LoadCommunityModeratorsPort;
import com.y11i.springcommddd.communities.application.port.out.SaveCommunityModeratorsPort;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModeratorRepository;
import com.y11i.springcommddd.iam.domain.MemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityModeratorPersistenceAdapter implements LoadCommunityModeratorsPort, SaveCommunityModeratorsPort {
    private final CommunityModeratorRepository communityModeratorRepository;
    /**
     * 주어진 커뮤니티의 모더레이터 목록을 조회합니다.
     *
     * @param communityId 커뮤니티 식별자
     * @return 해당 커뮤니티에 부여된 모더레이터 엔트리 목록
     */
    @Override
    public List<CommunityModerator> loadByCommunityId(CommunityId communityId) {
        return communityModeratorRepository.findByCommunityId(communityId);
    }

    @Override
    public List<CommunityModerator> loadByMemberId(MemberId memberId) {
        return communityModeratorRepository.findByMemberId(memberId);
    }

    /**
     * 커뮤니티 모더레이터 엔티티를 저장합니다.
     *
     * @param moderator 저장할 모더레이터 애그리게잇
     * @return 저장된 모더레이터 애그리게잇 (ID, createdAt 등이 채워진 상태)
     */
    @Override
    @Transactional
    public CommunityModerator save(CommunityModerator moderator) {
        return communityModeratorRepository.save(moderator);
    }

    @Override
    public void delete(CommunityModerator moderator) {
        communityModeratorRepository.delete(moderator);
    }
}
