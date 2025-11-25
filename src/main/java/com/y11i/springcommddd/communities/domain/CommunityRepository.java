package com.y11i.springcommddd.communities.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * {@link Community} 애그리게잇의 저장소 인터페이스.
 * <p>
 * 커뮤니티 도메인 객체의 영속성과 조회를 담당하며,
 * {@link CommunityRepository}의 구현체는 인프라스트럭처 계층에서 제공합니다.
 * </p>
 *
 * <p><b>책임:</b></p>
 * <ul>
 *     <li>커뮤니티의 생성 및 갱신</li>
 *     <li>식별자({@link CommunityId})로 단건 조회</li>
 *     <li>커뮤니티 고유 키({@link CommunityNameKey})로 조회</li>
 *     <li>모든 커뮤니티 목록 조회</li>
 * </ul>
 *
 * <p>
 * 도메인 계층에서는 이 인터페이스에만 의존하며,
 * 구체적인 저장 기술(JPA, MyBatis 등)에는 의존하지 않습니다.
 * </p>
 *
 * @author y11
 */
public interface CommunityRepository {

    /**
     * 커뮤니티를 저장하거나 수정합니다.
     *
     * @param c 저장할 {@link Community} 객체
     * @return 저장된 {@link Community} 인스턴스
     */
    Community save(Community c);

    /**
     * 식별자({@link CommunityId})를 통해 커뮤니티를 조회합니다.
     *
     * @param id 커뮤니티 식별자
     * @return 존재하면 {@link Community}를 포함하는 {@link Optional}, 없으면 비어 있음
     */
    Optional<Community> findById(CommunityId id);

    /**
     * 고유한 커뮤니티 이름 키({@link CommunityNameKey})로 커뮤니티를 조회합니다.
     *
     * @param key 커뮤니티 이름 키
     * @return 일치하는 커뮤니티가 존재하면 {@link Community}, 없으면 비어 있음
     */
    Optional<Community> findByCommunityNameKey(CommunityNameKey key);

    Optional<Community> findByCommunityName(CommunityName communityName);

    /**
     * 모든 커뮤니티를 조회합니다.
     *
     * @return 전체 {@link Community} 목록
     */
    List<Community> findAll();

    Page<Community> findByStatus(CommunityStatus status, Pageable pageable);

    long countByStatus(CommunityStatus status);
}
