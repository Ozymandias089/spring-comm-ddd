package com.y11i.springcommddd.communities.infrastructure;

import com.y11i.springcommddd.communities.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data JPA 기반의 커뮤니티 리포지토리.
 * <p>
 * {@link Community} 엔티티를 데이터베이스에 영속화하며,
 * 커뮤니티 이름 키({@link CommunityNameKey})로 조회하는 기능을 제공합니다.
 * </p>
 *
 * <p><b>역할:</b></p>
 * <ul>
 *     <li>Spring Data JPA가 자동 구현체를 생성</li>
 *     <li>쿼리 메서드 파생(query derivation)을 통해 엔티티 조회</li>
 *     <li>{@link CommunityRepositoryAdapter}에서 사용되어 도메인에 연결됨</li>
 * </ul>
 *
 * @see JpaRepository
 * @see CommunityRepositoryAdapter
 */
public interface JpaCommunityRepository extends JpaRepository<Community, CommunityId> {
    /**
     * 커뮤니티 이름 키({@link CommunityNameKey})를 통해 커뮤니티를 조회합니다.
     *
     * @param key 커뮤니티 이름 키
     * @return 일치하는 {@link Community}가 존재하면 반환, 없으면 빈 {@link Optional}
     */
    Optional<Community> findByCommunityNameKey(CommunityNameKey key);

    Optional<Community> findByCommunityName(CommunityName communityName);

    Page<Community> findByStatus(CommunityStatus status, Pageable pageable);

    long countByStatus(CommunityStatus status);

    @Query("""
           SELECT c FROM Community c
           WHERE c.status = :status
             AND (LOWER(c.communityNameKey.value) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.communityName.value) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """)
    Page<Community> searchByStatusAndKeyword(@Param("status") CommunityStatus status,
                                             @Param("keyword") String keyword,
                                             Pageable pageable);

    @Query("""
           SELECT COUNT(c) FROM Community c
           WHERE c.status = :status
             AND (LOWER(c.communityNameKey.value) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.communityName.value) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """)
    long countByStatusAndKeyword(@Param("status") CommunityStatus status,
                                 @Param("keyword") String keyword);
}
