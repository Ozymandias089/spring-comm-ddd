package com.y11i.springcommddd.communities.moderators.infrastructure;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModeratorRepository;
import com.y11i.springcommddd.iam.domain.MemberId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * {@link CommunityModeratorRepository}의 JPA 어댑터 구현.
 *
 * <p><b>트랜잭션 정책</b><br>
 * 클래스 레벨 기본은 읽기 전용이며, 쓰기 메서드(save/delete)에 개별적으로
 * {@link Transactional}을 부여합니다.
 * </p>
 */
@Repository
@Transactional(readOnly = true)
public class CommunityModeratorRepositoryAdapter implements CommunityModeratorRepository {
    private final JpaCommunityModeratorRepository jpaCommunityModeratorRepository;

    /**
     * 생성자.
     *
     * @param jpaCommunityModeratorRepository 내부 위임 대상 JPA 리포지토리
     */
    public CommunityModeratorRepositoryAdapter(JpaCommunityModeratorRepository jpaCommunityModeratorRepository) {
        this.jpaCommunityModeratorRepository = jpaCommunityModeratorRepository;
    }

    /** {@inheritDoc} */
    @Override @Transactional
    public CommunityModerator save(CommunityModerator mod) { return jpaCommunityModeratorRepository.save(mod); }

    /** {@inheritDoc} */
    @Override @Transactional
    public void delete(CommunityModerator mod) { jpaCommunityModeratorRepository.delete(mod); }

    /** {@inheritDoc} */
    @Override
    public boolean existsByCommunityIdAndMemberId(CommunityId communityId, MemberId memberId) {
        return jpaCommunityModeratorRepository.existsByCommunityIdAndMemberId(communityId, memberId);
    }

    /** {@inheritDoc} */
    @Override
    public List<CommunityModerator> findByCommunityId(CommunityId communityId) { return jpaCommunityModeratorRepository.findByCommunityId(communityId); }

    /** {@inheritDoc} */
    @Override
    public List<CommunityModerator> findByMemberId(MemberId memberId) { return jpaCommunityModeratorRepository.findByMemberId(memberId); }

    /** {@inheritDoc} */
    @Override
    public Optional<CommunityModerator> findOneByCommunityIdAndMemberId(CommunityId communityId, MemberId memberId) {
        return jpaCommunityModeratorRepository.findByCommunityIdAndMemberId(communityId, memberId);
    }
}
