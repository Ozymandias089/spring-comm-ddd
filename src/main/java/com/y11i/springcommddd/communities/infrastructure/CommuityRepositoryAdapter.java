package com.y11i.springcommddd.communities.infrastructure;

import com.y11i.springcommddd.communities.domain.Community;
import com.y11i.springcommddd.communities.domain.CommunityId;
import com.y11i.springcommddd.communities.domain.CommunityNameKey;
import com.y11i.springcommddd.communities.domain.CommunityRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * {@link CommunityRepository}의 인프라스트럭처 계층 구현체.
 * <p>
 * 이 클래스는 도메인 계층의 {@link CommunityRepository}를
 * JPA 기반 리포지토리인 {@link JpaCommunityRepository}로 어댑팅(연결)합니다.
 * </p>
 *
 * <p><b>특징:</b></p>
 * <ul>
 *     <li>도메인 계층은 JPA 세부 구현에 직접 의존하지 않음</li>
 *     <li>읽기 작업에는 {@code readOnly = true} 트랜잭션 적용</li>
 *     <li>쓰기 작업(저장)은 별도의 트랜잭션에서 수행</li>
 * </ul>
 *
 * @see JpaCommunityRepository
 * @author y11
 */
@Repository
@Transactional(readOnly = true)
public class CommuityRepositoryAdapter implements CommunityRepository {

    private final JpaCommunityRepository jpaCommunityRepository;

    /**
     * JPA 리포지토리를 주입받습니다.
     *
     * @param jpaCommunityRepository JPA 기반 커뮤니티 리포지토리
     */
    public CommuityRepositoryAdapter(JpaCommunityRepository jpaCommunityRepository) {
        this.jpaCommunityRepository = jpaCommunityRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public Community save(Community c) {
        return jpaCommunityRepository.save(c);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Community> findById(CommunityId id) {
        return jpaCommunityRepository.findById(id);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Community> findByCommunityNameKey(CommunityNameKey key) {
        return jpaCommunityRepository.findByCommunityNameKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public List<Community> findAll() {
        return jpaCommunityRepository.findAll();
    }
}
