package com.y11i.springcommddd.communities.moderators.domain;

import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.iam.domain.MemberId;

import java.util.List;
import java.util.Optional;

/**
 * 커뮤니티 모더레이터 리포지토리(도메인 계약).
 *
 * <p>모더레이터 권한의 생성/삭제 및 조회 기능을 제공합니다.</p>
 */
public interface CommunityModeratorRepository {

    /**
     * 모더레이터 권한을 저장합니다.
     *
     * @param mod 저장할 엔티티
     * @return 저장된 엔티티
     */
    CommunityModerator save(CommunityModerator mod);

    /**
     * 모더레이터 권한을 제거합니다.
     *
     * @param mod 삭제할 엔티티
     */
    void delete(CommunityModerator mod);

    /**
     * 특정 커뮤니티-회원 조합의 모더레이터 권한 존재 여부.
     *
     * @param communityId 커뮤니티 식별자
     * @param memberId    회원 식별자
     * @return 존재하면 {@code true}
     */
    boolean existsByCommunityIdAndMemberId(CommunityId communityId, MemberId memberId);

    /**
     * 커뮤니티에 속한 모든 모더레이터 목록을 조회합니다.
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
    Optional<CommunityModerator> findOneByCommunityIdAndMemberId(CommunityId communityId, MemberId memberId);
}
