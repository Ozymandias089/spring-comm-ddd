package com.y11i.springcommddd.communities.moderators.infrastructure;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModerator;
import com.y11i.springcommddd.communities.moderators.domain.CommunityModeratorId;
import com.y11i.springcommddd.iam.domain.MemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 커뮤니티 모더레이터용 Spring Data JPA 리포지토리.
 *
 * <p>테이블 {@code community_moderators}에 대한 CRUD 및 파생 쿼리를 제공합니다.</p>
 */
public interface JpaCommunityModeratorRepository extends JpaRepository<CommunityModerator, CommunityModeratorId> {
    /**
     * 특정 커뮤니티-회원 조합의 모더레이터 권한 존재 여부를 조회합니다.
     *
     * @param communityId 커뮤니티 식별자
     * @param memberId    회원 식별자
     * @return 존재 시 {@code true}
     */
    boolean existsByCommunityIdAndMemberId(CommunityId communityId, MemberId memberId);

    /**
     * 커뮤니티에 속한 모더레이터 목록을 조회합니다.
     *
     * @param communityId 커뮤니티 식별자
     * @return 모더레이터 목록
     */
    List<CommunityModerator> findByCommunityId(CommunityId communityId);

    /**
     * 특정 회원이 가진 모더레이터 권한 목록을 조회합니다.
     *
     * @param memberId 회원 식별자
     * @return 모더레이터 목록
     */
    List<CommunityModerator> findByMemberId(MemberId memberId);

    /**
     * 특정 커뮤니티-회원 조합의 모더레이터 엔티티를 단건 조회합니다.
     *
     * @param communityId 커뮤니티 식별자
     * @param memberId    회원 식별자
     * @return 존재 시 {@link Optional}로 래핑된 엔티티
     */
    Optional<CommunityModerator> findByCommunityIdAndMemberId(CommunityId communityId, MemberId memberId);
}
